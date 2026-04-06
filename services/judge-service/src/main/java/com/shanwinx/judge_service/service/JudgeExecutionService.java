package com.shanwinx.judge_service.service;

import com.shanwinx.judge_service.dto.JudgeResultDTO;
import com.shanwinx.judge_service.dto.SubmissionJobDTO;
import com.shanwinx.judge_service.dto.Verdict;
import com.shanwinx.judge_service.service.executor.LanguageExecutor;
import com.shanwinx.judge_service.service.executor.LanguageExecutorResolver;
import java.nio.file.Path;
import java.util.List;
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
        DockerSandboxService.ProcessOutcome outcome = dockerSandboxService.executeCommand(command, workingDirectory, timeoutMs, phase);
        return new ProcessOutcome(
                outcome.getExitCode(),
                outcome.getStdout(),
                outcome.getStderr(),
                outcome.isTimedOut(),
                outcome.getTimeUsed(),
                outcome.getMemoryUsed());
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
