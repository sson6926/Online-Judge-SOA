package com.shanwinx.judge_service.service;

import com.shanwinx.judge_service.dto.JudgeResultDTO;
import com.shanwinx.judge_service.dto.SubmissionJobDTO;
import com.shanwinx.judge_service.dto.Verdict;
import com.shanwinx.judge_service.service.executor.LanguageExecutor;
import com.shanwinx.judge_service.service.executor.LanguageExecutorResolver;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class JudgeExecutionService {

    private static final long COMPILE_TIMEOUT_MS = 10_000;
    private static final long RUN_TIMEOUT_MS = 5_000;

    private final JobWorkspaceService jobWorkspaceService;
    private final LanguageExecutorResolver languageExecutorResolver;
    private final DockerSandboxService dockerSandboxService;

    public JudgeResultDTO execute(SubmissionJobDTO job, Path workspacePath) {
        return executeDetailed(job, workspacePath).getJudgeResult();
    }

    public ExecutionResult executeDetailed(SubmissionJobDTO job, Path workspacePath) {
        LanguageExecutor executor = languageExecutorResolver.resolve(job.getLanguage());
        if (executor == null) {
            JudgeResultDTO result = JudgeResultDTO.builder()
                    .submissionId(job.getId())
                    .verdict(Verdict.CE)
                    .timeUsed(0)
                    .memoryUsed(0)
                    .errorDetails("Unsupported language: " + job.getLanguage())
                    .build();
            return new ExecutionResult(result, "", "Unsupported language: " + job.getLanguage());
        }

        Path sourceFilePath = jobWorkspaceService.writeSourceFile(workspacePath, job, executor.sourceFileName());
        log.info("Wrote source file for submissionId={} to {}", job.getId(), sourceFilePath);

        if (executor.requiresCompilation()) {
            ProcessOutcome compileOutcome = runCommand(
                    executor.compileCommand(workspacePath, sourceFilePath),
                    workspacePath,
                COMPILE_TIMEOUT_MS,
                "compile");

            if (compileOutcome.timedOut) {
                JudgeResultDTO result = JudgeResultDTO.builder()
                        .submissionId(job.getId())
                        .verdict(Verdict.CE)
                .timeUsed(compileOutcome.timeUsed)
                .memoryUsed(compileOutcome.memoryUsed)
                        .errorDetails("Compilation timeout")
                        .build();
                return new ExecutionResult(result, compileOutcome.stdout, compileOutcome.stderr);
            }

            if (compileOutcome.exitCode != 0) {
                JudgeResultDTO result = JudgeResultDTO.builder()
                        .submissionId(job.getId())
                        .verdict(Verdict.CE)
                .timeUsed(compileOutcome.timeUsed)
                .memoryUsed(compileOutcome.memoryUsed)
                        .errorDetails(trimError(compileOutcome.stderr))
                        .build();
                return new ExecutionResult(result, compileOutcome.stdout, compileOutcome.stderr);
            }
        }

        ProcessOutcome runOutcome = runCommand(
            executor.runCommand(workspacePath, sourceFilePath),
            workspacePath,
            RUN_TIMEOUT_MS,
            "run");

        if (runOutcome.timedOut) {
            JudgeResultDTO result = JudgeResultDTO.builder()
                    .submissionId(job.getId())
                    .verdict(Verdict.TLE)
                .timeUsed(runOutcome.timeUsed)
                .memoryUsed(runOutcome.memoryUsed)
                    .errorDetails("Execution timeout")
                    .build();
            return new ExecutionResult(result, runOutcome.stdout, runOutcome.stderr);
        }

        Verdict verdict = runOutcome.exitCode == 0 ? Verdict.AC : Verdict.RTE;
        JudgeResultDTO result = JudgeResultDTO.builder()
                .submissionId(job.getId())
                .verdict(verdict)
            .timeUsed(runOutcome.timeUsed)
            .memoryUsed(runOutcome.memoryUsed)
                .errorDetails(runOutcome.exitCode == 0 ? null : trimError(runOutcome.stderr))
                .build();
        return new ExecutionResult(result, runOutcome.stdout, runOutcome.stderr);
    }

    private ProcessOutcome runCommand(List<String> command, Path workingDirectory, long timeoutMs, String phase) {
        if (command == null || command.isEmpty()) {
            return new ProcessOutcome(0, "", "", false, 0, 0);
        }

        String statFileName = phase + ".stat";
        Path statFilePath = workingDirectory.resolve(statFileName);
        List<String> dockerCommand = dockerSandboxService.buildDockerCommand(command, workingDirectory, statFileName);
        try {
            ProcessBuilder builder = new ProcessBuilder(dockerCommand);

            Process process = builder.start();
            boolean completed = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);

            if (!completed) {
                process.destroyForcibly();
                ExecutionStat stat = parseExecutionStat(statFilePath);
                return new ProcessOutcome(-1, "", "timeout", true, stat.timeUsed, stat.memoryUsed);
            }

            String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            ExecutionStat stat = parseExecutionStat(statFilePath);
            return new ProcessOutcome(process.exitValue(), stdout, stderr, false, stat.timeUsed, stat.memoryUsed);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot execute command in sandbox: " + String.join(" ", dockerCommand), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Command interrupted: " + String.join(" ", dockerCommand), e);
        }
    }

    private String trimError(String message) {
        if (message == null || message.isBlank()) {
            return "Execution failed";
        }

        String normalized = message.strip();
        int maxLength = 2000;
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength);
    }

    private ExecutionStat parseExecutionStat(Path statFilePath) {
        if (statFilePath == null || !Files.exists(statFilePath)) {
            return new ExecutionStat(0, 0);
        }

        try {
            String statRaw = Files.readString(statFilePath, StandardCharsets.UTF_8).trim();
            if (statRaw.isBlank()) {
                return new ExecutionStat(0, 0);
            }

            String[] parts = statRaw.split("\\|");
            if (parts.length < 2) {
                return new ExecutionStat(0, 0);
            }

            int timeUsed = parseSecondsToMillis(parts[0]);
            int memoryUsed = parseInteger(parts[1]);
            return new ExecutionStat(timeUsed, memoryUsed);
        } catch (IOException ex) {
            log.warn("Cannot read stat file {}", statFilePath, ex);
            return new ExecutionStat(0, 0);
        }
    }

    private int parseSecondsToMillis(String secondsText) {
        try {
            double seconds = Double.parseDouble(secondsText.trim());
            return (int) Math.max(0, Math.round(seconds * 1000));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private int parseInteger(String value) {
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static final class ProcessOutcome {
        private final int exitCode;
        private final String stdout;
        private final String stderr;
        private final boolean timedOut;
        private final int timeUsed;
        private final int memoryUsed;

        private ProcessOutcome(int exitCode, String stdout, String stderr, boolean timedOut, int timeUsed, int memoryUsed) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
            this.timedOut = timedOut;
            this.timeUsed = timeUsed;
            this.memoryUsed = memoryUsed;
        }
    }

    private static final class ExecutionStat {
        private final int timeUsed;
        private final int memoryUsed;

        private ExecutionStat(int timeUsed, int memoryUsed) {
            this.timeUsed = timeUsed;
            this.memoryUsed = memoryUsed;
        }
    }

    public static final class ExecutionResult {
        private final JudgeResultDTO judgeResult;
        private final String stdout;
        private final String stderr;

        public ExecutionResult(JudgeResultDTO judgeResult, String stdout, String stderr) {
            this.judgeResult = judgeResult;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public JudgeResultDTO getJudgeResult() {
            return judgeResult;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }
    }
}
