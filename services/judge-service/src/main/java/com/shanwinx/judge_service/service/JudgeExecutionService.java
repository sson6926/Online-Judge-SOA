package com.shanwinx.judge_service.service;

import com.shanwinx.judge_service.dto.JudgeResultDTO;
import com.shanwinx.judge_service.dto.RunCodeResponseDTO;
import com.shanwinx.judge_service.dto.SubmissionJobDTO;
import com.shanwinx.judge_service.dto.Verdict;
import com.shanwinx.judge_service.service.executor.CppLanguageExecutor;
import com.shanwinx.judge_service.service.executor.JavaLanguageExecutor;
import com.shanwinx.judge_service.service.executor.LanguageExecutor;
import com.shanwinx.judge_service.service.executor.PythonLanguageExecutor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
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
    private final JavaLanguageExecutor javaLanguageExecutor;
    private final PythonLanguageExecutor pythonLanguageExecutor;
    private final CppLanguageExecutor cppLanguageExecutor;
    @Value("${judge.sandbox.image:judge-sandbox:latest}")
    private String sandboxImage;

    public JudgeResultDTO execute(SubmissionJobDTO job, Path workspacePath) {
        return executeDetailed(job, workspacePath).getJudgeResult();
        }

        public ExecutionResult executeDetailed(SubmissionJobDTO job, Path workspacePath) {
        LanguageExecutor executor = resolveExecutor(job.getLanguage());
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
                    COMPILE_TIMEOUT_MS);

            if (compileOutcome.timedOut) {
                JudgeResultDTO result = JudgeResultDTO.builder()
                        .submissionId(job.getId())
                        .verdict(Verdict.CE)
                        .timeUsed(0)
                        .memoryUsed(0)
                        .errorDetails("Compilation timeout")
                        .build();
                return new ExecutionResult(result, compileOutcome.stdout, compileOutcome.stderr);
            }

            if (compileOutcome.exitCode != 0) {
                JudgeResultDTO result = JudgeResultDTO.builder()
                        .submissionId(job.getId())
                        .verdict(Verdict.CE)
                        .timeUsed(0)
                        .memoryUsed(0)
                        .errorDetails(trimError(compileOutcome.stderr))
                        .build();
                return new ExecutionResult(result, compileOutcome.stdout, compileOutcome.stderr);
            }
        }

        ProcessOutcome runOutcome = runCommand(executor.runCommand(workspacePath, sourceFilePath), workspacePath, RUN_TIMEOUT_MS);

        if (runOutcome.timedOut) {
            JudgeResultDTO result = JudgeResultDTO.builder()
                    .submissionId(job.getId())
                    .verdict(Verdict.TLE)
                    .timeUsed(0)
                    .memoryUsed(0)
                    .errorDetails("Execution timeout")
                    .build();
            return new ExecutionResult(result, runOutcome.stdout, runOutcome.stderr);
        }

        Verdict verdict = runOutcome.exitCode == 0 ? Verdict.AC : Verdict.RTE;
        JudgeResultDTO result = JudgeResultDTO.builder()
                .submissionId(job.getId())
                .verdict(verdict)
                .timeUsed(0)
                .memoryUsed(0)
                .errorDetails(runOutcome.exitCode == 0 ? null : trimError(runOutcome.stderr))
                .build();
        return new ExecutionResult(result, runOutcome.stdout, runOutcome.stderr);
    }

    public RunCodeResponseDTO toRunCodeResponse(ExecutionResult executionResult) {
        JudgeResultDTO result = executionResult.getJudgeResult();
        return RunCodeResponseDTO.builder()
                .submissionId(result.getSubmissionId())
                .verdict(result.getVerdict())
                .timeUsed(result.getTimeUsed())
                .memoryUsed(result.getMemoryUsed())
                .errorDetails(result.getErrorDetails())
                .stdout(executionResult.getStdout())
                .stderr(executionResult.getStderr())
                .build();
    }

    private LanguageExecutor resolveExecutor(String language) {
        if (language == null) {
            return null;
        }

        String normalized = language.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "java" -> javaLanguageExecutor;
            case "python", "py" -> pythonLanguageExecutor;
            case "cpp", "c++" -> cppLanguageExecutor;
            default -> null;
        };
    }

    private ProcessOutcome runCommand(List<String> command, Path workingDirectory, long timeoutMs) {
        if (command == null || command.isEmpty()) {
            return new ProcessOutcome(0, "", "", false);
        }

        List<String> dockerCommand = buildDockerCommand(command, workingDirectory);
        try {
            ProcessBuilder builder = new ProcessBuilder(dockerCommand);

            Process process = builder.start();
            boolean completed = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);

            if (!completed) {
                process.destroyForcibly();
                return new ProcessOutcome(-1, "", "timeout", true);
            }

            String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            return new ProcessOutcome(process.exitValue(), stdout, stderr, false);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot execute command in sandbox: " + String.join(" ", dockerCommand), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Command interrupted: " + String.join(" ", dockerCommand), e);
        }
    }

    private List<String> buildDockerCommand(List<String> command, Path workingDirectory) {
        String workspacePath = workingDirectory.toAbsolutePath().normalize().toString();
        String shellCommand = buildShellCommand(command);
        List<String> dockerCommand = new ArrayList<>();
        dockerCommand.add("docker");
        dockerCommand.add("run");
        dockerCommand.add("--rm");
        dockerCommand.add("--network");
        dockerCommand.add("none");
        dockerCommand.add("-v");
        dockerCommand.add(workspacePath + ":/workspace");
        dockerCommand.add("-w");
        dockerCommand.add("/workspace");
        dockerCommand.add("--entrypoint");
        dockerCommand.add("/bin/bash");
        dockerCommand.add(sandboxImage);
        dockerCommand.add("-lc");
        dockerCommand.add(shellCommand);
        return dockerCommand;
    }

    private String buildShellCommand(List<String> command) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < command.size(); i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(shellEscape(command.get(i)));
        }
        return builder.toString();
    }

    private String shellEscape(String value) {
        if (value == null) {
            return "''";
        }
        return "'" + value.replace("'", "'\"'\"'") + "'";
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

    private static final class ProcessOutcome {
        private final int exitCode;
        private final String stdout;
        private final String stderr;
        private final boolean timedOut;

        private ProcessOutcome(int exitCode, String stdout, String stderr, boolean timedOut) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
            this.timedOut = timedOut;
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
