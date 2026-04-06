package com.shanwinx.judge_service.service;

import com.shanwinx.judge_service.client.ProblemServiceClient;
import com.shanwinx.judge_service.dto.JudgeResultDTO;
import com.shanwinx.judge_service.dto.ListTestCaseDto;
import com.shanwinx.judge_service.dto.ListTestCaseMockData;
import com.shanwinx.judge_service.dto.SubmissionJobDTO;
import com.shanwinx.judge_service.dto.Verdict;
import com.shanwinx.judge_service.service.executor.LanguageExecutor;
import com.shanwinx.judge_service.service.executor.LanguageExecutorResolver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String USER_OUTPUT_FILE = "output.user.out";
    private static final String EXPECTED_OUTPUT_FILE = "output.expected.out";
    private static final String STAT_FILE = "stat";

    private static final String TESTCASE_SOURCE_PROBLEM = "problem";

    private final JobWorkspaceService jobWorkspaceService;
    private final LanguageExecutorResolver languageExecutorResolver;
    private final DockerSandboxService dockerSandboxService;
    private final TestCaseService testCaseService;
    private final ProblemServiceClient problemServiceClient;

    @Value("${judge.testcases.source:mock}")
    private String testcasesSource;

    public JudgeResultDTO execute(SubmissionJobDTO job, Path workspacePath) {
        return executeDetailed(job, workspacePath).getJudgeResult();
    }

    public ExecutionResult executeDetailed(SubmissionJobDTO job, Path workspacePath) {
        LanguageExecutor executor = languageExecutorResolver.resolve(job.getLanguage());
        if (executor == null) {
            return executionResult(
                    job.getId(),
                    Verdict.SE,
                    0,
                    0,
                    "Unsupported language: " + job.getLanguage());
        }

        if (isProblemSource() && job.getProblemId() == null) {
            return executionResult(job.getId(), Verdict.SE, 0, 0, "Missing problem id");
        }

        List<ListTestCaseDto> testcases = loadTestCases(job);
        if (testcases.isEmpty()) {
            return executionResult(job.getId(), Verdict.SE, 0, 0, "No test cases for problem");
        }

        Path sourceFilePath = jobWorkspaceService.writeSourceFile(workspacePath, job, executor.sourceFileName());
        log.info("Wrote source file for submissionId={} to {}", job.getId(), sourceFilePath);

        testCaseService.prepareTestCases(job.getId(), workspacePath, testcases);

        if (executor.requiresCompilation()) {
            int compileExitCode =
                    dockerSandboxService.compile(executor, workspacePath, sourceFilePath, COMPILE_TIMEOUT_MS);

            if (compileExitCode == -1) {
                return executionResult(job.getId(), Verdict.CE, 0, 0, "Compilation timeout");
            }
            if (compileExitCode != 0) {
                return executionResult(job.getId(), Verdict.CE, 0, 0, "Compilation failed");
            }
        }

        int maxTimeUsedMs = 0;
        int maxMemoryUsedKb = 0;

        for (ListTestCaseDto testCase : testcases) {
            String testId = testCase.getId().toString();
            long runTimeoutMs = runTimeoutMsFor(testCase);

            int runExitCode =
                    dockerSandboxService.run(executor, workspacePath, sourceFilePath, testId, runTimeoutMs);

            Path statPath = testCaseFile(workspacePath, testId, STAT_FILE);
            SandboxMetrics metrics = readSandboxMetrics(statPath);
            if (metrics != null) {
                int elapsedMs = (int) Math.round(metrics.elapsedSeconds() * 1000.0);
                int memKb = (int) metrics.maxRssKb();
                maxTimeUsedMs = Math.max(maxTimeUsedMs, elapsedMs);
                maxMemoryUsedKb = Math.max(maxMemoryUsedKb, memKb);
            }

            if (runExitCode == -1) {
                return executionResult(
                        job.getId(),
                        Verdict.TLE,
                        maxTimeUsedMs,
                        maxMemoryUsedKb,
                        "Time limit exceeded (test case " + testId + ")");
            }
            if (runExitCode != 0) {
                return executionResult(
                        job.getId(),
                        Verdict.RTE,
                        maxTimeUsedMs,
                        maxMemoryUsedKb,
                        "Runtime error (test case " + testId + ")");
            }

            if (testCase.getTimeLimit() != null
                    && testCase.getTimeLimit() > 0
                    && metrics != null) {
                int elapsedMs = (int) Math.round(metrics.elapsedSeconds() * 1000.0);
                if (elapsedMs > testCase.getTimeLimit()) {
                    return executionResult(
                            job.getId(),
                            Verdict.TLE,
                            maxTimeUsedMs,
                            maxMemoryUsedKb,
                            "Time limit exceeded (test case " + testId + ")");
                }
            }

            if (testCase.getMemoryLimit() != null && testCase.getMemoryLimit() > 0 && metrics != null) {
                long limitKb = testCase.getMemoryLimit().longValue() * 1024L;
                if (metrics.maxRssKb() > limitKb) {
                    return executionResult(
                            job.getId(),
                            Verdict.MLE,
                            maxTimeUsedMs,
                            maxMemoryUsedKb,
                            "Memory limit exceeded (test case " + testId + ")");
                }
            }

            try {
                if (!outputsMatch(workspacePath, testId)) {
                    return executionResult(
                            job.getId(),
                            Verdict.WA,
                            maxTimeUsedMs,
                            maxMemoryUsedKb,
                            "Wrong answer (test case " + testId + ")");
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot compare outputs for test case " + testId, e);
            }
        }

        return executionResult(job.getId(), Verdict.AC, maxTimeUsedMs, maxMemoryUsedKb, null);
    }

    private boolean isProblemSource() {
        return testcasesSource != null && TESTCASE_SOURCE_PROBLEM.equalsIgnoreCase(testcasesSource.trim());
    }

    private List<ListTestCaseDto> loadTestCases(SubmissionJobDTO job) {
        if (isProblemSource()) {
            List<ListTestCaseDto> fromApi = problemServiceClient.getTestCasesByProblemId(job.getProblemId());
            return fromApi != null ? fromApi : List.of();
        }
        return ListTestCaseMockData.sample();
    }

    private static long runTimeoutMsFor(ListTestCaseDto testCase) {
        int limit = testCase.getTimeLimit() != null && testCase.getTimeLimit() > 0
                ? testCase.getTimeLimit()
                : (int) DEFAULT_RUN_TIMEOUT_MS;
        return Math.min(MAX_RUN_TIMEOUT_MS, limit + RUN_TIMEOUT_BUFFER_MS);
    }

    private static Path testCaseFile(Path workspacePath, String testId, String fileName) {
        return workspacePath.resolve(TESTCASES_DIR).resolve(testId).resolve(fileName);
    }

    private static boolean outputsMatch(Path workspacePath, String testId) throws IOException {
        Path userOut = testCaseFile(workspacePath, testId, USER_OUTPUT_FILE);
        Path expectedOut = testCaseFile(workspacePath, testId, EXPECTED_OUTPUT_FILE);
        String actual = Files.readString(userOut);
        String expected = Files.readString(expectedOut);
        return normalizeOutput(actual).equals(normalizeOutput(expected));
    }

    static String normalizeOutput(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\r\n", "\n").replace('\r', '\n').stripTrailing();
    }

    private static SandboxMetrics readSandboxMetrics(Path statFile) {
        try {
            if (!Files.isRegularFile(statFile)) {
                return null;
            }
            String content = Files.readString(statFile).trim();
            if (content.isEmpty()) {
                return null;
            }
            int sep = content.indexOf('|');
            if (sep < 0) {
                return null;
            }
            double elapsed = Double.parseDouble(content.substring(0, sep).trim());
            long rssKb = Long.parseLong(content.substring(sep + 1).trim());
            return new SandboxMetrics(elapsed, rssKb);
        } catch (IOException | NumberFormatException e) {
            return null;
        }
    }

    private static ExecutionResult executionResult(
            UUID submissionId, Verdict verdict, int timeUsedMs, int memoryUsedKb, String errorDetails) {
        JudgeResultDTO dto = JudgeResultDTO.builder()
                .submissionId(submissionId)
                .verdict(verdict)
                .timeUsed(timeUsedMs)
                .memoryUsed(memoryUsedKb)
                .errorDetails(errorDetails)
                .build();
        return new ExecutionResult(dto, "", errorDetails != null ? errorDetails : "");
    }

    private record SandboxMetrics(double elapsedSeconds, long maxRssKb) {}

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
