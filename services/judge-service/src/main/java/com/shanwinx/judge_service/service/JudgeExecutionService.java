package com.shanwinx.judge_service.service;

import com.shanwinx.judge_service.dto.JudgeResultDTO;
import com.shanwinx.judge_service.dto.ListTestCaseDto;
import com.shanwinx.judge_service.dto.SubmissionJobDTO;
import com.shanwinx.judge_service.dto.Verdict;
import com.shanwinx.judge_service.service.SandboxMetricsReader.Metrics;
import com.shanwinx.judge_service.service.executor.LanguageExecutor;
import com.shanwinx.judge_service.service.executor.LanguageExecutorResolver;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class JudgeExecutionService {

    private static final long COMPILE_TIMEOUT_MS = 10_000;
    private static final long DEFAULT_RUN_TIMEOUT_MS = 5_000;
    private static final long MAX_RUN_TIMEOUT_MS = 60_000;
    private static final long RUN_TIMEOUT_BUFFER_MS = 500;

    private static final String TESTCASES_DIR = "testcases";
    private static final String STAT_FILE = "stat";
    private static final String USER_OUTPUT_FILE = "output.user.out";
    private static final String EXPECTED_OUTPUT_FILE = "output.expected.out";

    private final JobWorkspaceService jobWorkspaceService;
    private final LanguageExecutorResolver languageExecutorResolver;
    private final DockerSandboxService dockerSandboxService;
    private final TestCaseService testCaseService;
    private final TestCaseLoader testCaseLoader;
    private final OutputComparator outputComparator;
    private final SandboxMetricsReader metricsReader;

    public JudgeResultDTO execute(SubmissionJobDTO job, Path workspacePath) {
        LanguageExecutor executor = languageExecutorResolver.resolve(job.getLanguage());
        if (executor == null) {
            return result(job.getId(), Verdict.SE, 0, 0, "Unsupported language: " + job.getLanguage());
        }

        if (testCaseLoader.isProblemSource() && job.getProblemId() == null) {
            return result(job.getId(), Verdict.SE, 0, 0, "Missing problem id");
        }

        List<ListTestCaseDto> testcases = testCaseLoader.load(job);
        if (testcases.isEmpty()) {
            return result(job.getId(), Verdict.SE, 0, 0, "No test cases for problem");
        }

        Path sourceFile = jobWorkspaceService.writeSourceFile(workspacePath, job, executor.sourceFileName());
        log.info("Wrote source file for submissionId={} at {}", job.getId(), sourceFile);

        testCaseService.prepareTestCases(job.getId(), workspacePath, testcases);

        if (executor.requiresCompilation()) {
            int exitCode = dockerSandboxService.compile(executor, workspacePath, sourceFile, COMPILE_TIMEOUT_MS);
            if (exitCode == -1) return result(job.getId(), Verdict.CE, 0, 0, "Compilation timeout");
            if (exitCode != 0)  return result(job.getId(), Verdict.CE, 0, 0, "Compilation failed");
        }

        return runTestCases(job.getId(), testcases, executor, workspacePath, sourceFile);
    }

    // --- private helpers ---

    private JudgeResultDTO runTestCases(
            UUID submissionId,
            List<ListTestCaseDto> testcases,
            LanguageExecutor executor,
            Path workspacePath,
            Path sourceFile) {

        int maxTimeMs = 0;
        int maxMemKb = 0;

        for (ListTestCaseDto tc : testcases) {
            String testId = tc.getId().toString();
            long timeoutMs = resolveTimeout(tc);

            int exitCode = dockerSandboxService.run(executor, workspacePath, sourceFile, testId, timeoutMs);

            Metrics metrics = metricsReader.read(testCaseFile(workspacePath, testId, STAT_FILE));
            if (metrics != null) {
                maxTimeMs = Math.max(maxTimeMs, metrics.elapsedMs());
                maxMemKb  = Math.max(maxMemKb,  (int) metrics.maxRssKb());
            }

            if (exitCode == -1) {
                return result(submissionId, Verdict.TLE, maxTimeMs, maxMemKb,
                        "Time limit exceeded (test case " + testId + ")");
            }
            if (exitCode != 0) {
                return result(submissionId, Verdict.RTE, maxTimeMs, maxMemKb,
                        "Runtime error (test case " + testId + ")");
            }

            if (metrics != null && isTimeLimitExceeded(tc, metrics)) {
                return result(submissionId, Verdict.TLE, maxTimeMs, maxMemKb,
                        "Time limit exceeded (test case " + testId + ")");
            }

            if (metrics != null && isMemoryLimitExceeded(tc, metrics)) {
                return result(submissionId, Verdict.MLE, maxTimeMs, maxMemKb,
                        "Memory limit exceeded (test case " + testId + ")");
            }

            try {
                if (!outputComparator.matches(
                        testCaseFile(workspacePath, testId, USER_OUTPUT_FILE),
                        testCaseFile(workspacePath, testId, EXPECTED_OUTPUT_FILE))) {
                    return result(submissionId, Verdict.WA, maxTimeMs, maxMemKb,
                            "Wrong answer (test case " + testId + ")");
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot compare outputs for test case " + testId, e);
            }
        }

        return result(submissionId, Verdict.AC, maxTimeMs, maxMemKb, null);
    }

    private static boolean isTimeLimitExceeded(ListTestCaseDto tc, Metrics metrics) {
        return tc.getTimeLimit() != null && tc.getTimeLimit() > 0
                && metrics.elapsedMs() > tc.getTimeLimit();
    }

    private static boolean isMemoryLimitExceeded(ListTestCaseDto tc, Metrics metrics) {
        if (tc.getMemoryLimit() == null || tc.getMemoryLimit() <= 0) return false;
        long limitKb = tc.getMemoryLimit().longValue() * 1024L;
        return metrics.maxRssKb() > limitKb;
    }

    private static long resolveTimeout(ListTestCaseDto tc) {
        int limit = (tc.getTimeLimit() != null && tc.getTimeLimit() > 0)
                ? tc.getTimeLimit()
                : (int) DEFAULT_RUN_TIMEOUT_MS;
        return Math.min(MAX_RUN_TIMEOUT_MS, limit + RUN_TIMEOUT_BUFFER_MS);
    }

    private static Path testCaseFile(Path workspacePath, String testId, String fileName) {
        return workspacePath.resolve(TESTCASES_DIR).resolve(testId).resolve(fileName);
    }

    private static JudgeResultDTO result(UUID id, Verdict verdict, int timeMs, int memKb, String error) {
        return JudgeResultDTO.builder()
                .submissionId(id)
                .verdict(verdict)
                .timeUsed(timeMs)
                .memoryUsed(memKb)
                .errorDetails(error)
                .build();
    }
}
