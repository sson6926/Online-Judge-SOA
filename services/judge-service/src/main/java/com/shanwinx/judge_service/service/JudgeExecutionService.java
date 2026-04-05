package com.shanwinx.judge_service.service;

import com.shanwinx.judge_service.dto.JudgeResultDTO;
import com.shanwinx.judge_service.dto.SubmissionJobDTO;
import com.shanwinx.judge_service.dto.Verdict;
import com.shanwinx.judge_service.service.executor.CppLanguageExecutor;
import com.shanwinx.judge_service.service.executor.JavaLanguageExecutor;
import com.shanwinx.judge_service.service.executor.LanguageExecutor;
import com.shanwinx.judge_service.service.executor.PythonLanguageExecutor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
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

    public JudgeResultDTO execute(SubmissionJobDTO job, Path workspacePath) {
        LanguageExecutor executor = resolveExecutor(job.getLanguage());
        if (executor == null) {
            return JudgeResultDTO.builder()
                    .submissionId(job.getId())
                    .verdict(Verdict.CE)
                    .timeUsed(0)
                    .memoryUsed(0)
                    .errorDetails("Unsupported language: " + job.getLanguage())
                    .build();
        }

        Path sourceFilePath = jobWorkspaceService.writeSourceFile(workspacePath, job, executor.sourceFileName());
        log.info("Wrote source file for submissionId={} to {}", job.getId(), sourceFilePath);

        if (executor.requiresCompilation()) {
            ProcessOutcome compileOutcome = runCommand(
                    executor.compileCommand(workspacePath, sourceFilePath),
                    workspacePath,
                    COMPILE_TIMEOUT_MS);

            if (compileOutcome.timedOut) {
                return JudgeResultDTO.builder()
                        .submissionId(job.getId())
                        .verdict(Verdict.CE)
                        .timeUsed(Math.toIntExact(compileOutcome.durationMs))
                        .memoryUsed(0)
                        .errorDetails("Compilation timeout")
                        .build();
            }

            if (compileOutcome.exitCode != 0) {
                return JudgeResultDTO.builder()
                        .submissionId(job.getId())
                        .verdict(Verdict.CE)
                        .timeUsed(Math.toIntExact(compileOutcome.durationMs))
                        .memoryUsed(0)
                        .errorDetails(trimError(compileOutcome.stderr))
                        .build();
            }
        }

        ProcessOutcome runOutcome = runCommand(executor.runCommand(workspacePath, sourceFilePath), workspacePath, RUN_TIMEOUT_MS);

        if (runOutcome.timedOut) {
            return JudgeResultDTO.builder()
                    .submissionId(job.getId())
                    .verdict(Verdict.TLE)
                    .timeUsed(Math.toIntExact(runOutcome.durationMs))
                    .memoryUsed(0)
                    .errorDetails("Execution timeout")
                    .build();
        }

        Verdict verdict = runOutcome.exitCode == 0 ? Verdict.AC : Verdict.RTE;
        return JudgeResultDTO.builder()
                .submissionId(job.getId())
                .verdict(verdict)
                .timeUsed(Math.toIntExact(runOutcome.durationMs))
                .memoryUsed(0)
                .errorDetails(runOutcome.exitCode == 0 ? null : trimError(runOutcome.stderr))
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
            return new ProcessOutcome(0, "", "", 0, false);
        }

        long startedAt = System.nanoTime();
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(workingDirectory.toFile());

            Process process = builder.start();
            boolean completed = process.waitFor(Duration.ofMillis(timeoutMs));

            if (!completed) {
                process.destroyForcibly();
                long durationMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
                return new ProcessOutcome(-1, "", "timeout", durationMs, true);
            }

            String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            long durationMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            return new ProcessOutcome(process.exitValue(), stdout, stderr, durationMs, false);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot execute command: " + String.join(" ", command), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Command interrupted: " + String.join(" ", command), e);
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

    private static final class ProcessOutcome {
        private final int exitCode;
        private final String stdout;
        private final String stderr;
        private final long durationMs;
        private final boolean timedOut;

        private ProcessOutcome(int exitCode, String stdout, String stderr, long durationMs, boolean timedOut) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
            this.durationMs = durationMs;
            this.timedOut = timedOut;
        }
    }
}
