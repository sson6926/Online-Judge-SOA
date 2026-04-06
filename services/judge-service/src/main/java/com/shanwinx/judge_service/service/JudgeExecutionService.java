package com.shanwinx.judge_service.service;

import com.shanwinx.judge_service.dto.JudgeResultDTO;
import com.shanwinx.judge_service.dto.ListTestCaseDto;
import com.shanwinx.judge_service.dto.ListTestCaseMockData;
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
    private final TestCaseService testCaseService;

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
            int compileExitCode = runCompileCommand(
                    executor,
                    workspacePath,
                    sourceFilePath,
                    COMPILE_TIMEOUT_MS);

            if (compileExitCode == -1) {
                JudgeResultDTO result = JudgeResultDTO.builder()
                        .submissionId(job.getId())
                        .verdict(Verdict.CE)
                        .timeUsed(0)
                        .memoryUsed(0)
                        .errorDetails("Compilation timeout")
                        .build();
                return new ExecutionResult(result, "", "");
            }

            if (compileExitCode != 0) {
                JudgeResultDTO result = JudgeResultDTO.builder()
                        .submissionId(job.getId())
                        .verdict(Verdict.CE)
                        .timeUsed(0)
                        .memoryUsed(0)
                        .errorDetails("Compilation failed")
                        .build();
                return new ExecutionResult(result, "", "");
            }
        }

        List<ListTestCaseDto> testcases = ListTestCaseMockData.sample();

        for (ListTestCaseDto testCase : testcases) {
            testCaseService.prepareTestCase(job.getId(), workspacePath, testCase);

            int runExitCode = runCommand(
                    executor,
                    workspacePath,
                    sourceFilePath,
                    testCase.getId().toString(),
                    RUN_TIMEOUT_MS);

            if (runExitCode == -1) {
                JudgeResultDTO result = JudgeResultDTO.builder()
                        .submissionId(job.getId())
                        .verdict(Verdict.TLE)
                        .timeUsed(0)
                        .memoryUsed(0)
                        .errorDetails("Execution timeout")
                        .build();
                return new ExecutionResult(result, "", "");
            }

            if (runExitCode != 0) {
                JudgeResultDTO result = JudgeResultDTO.builder()
                        .submissionId(job.getId())
                        .verdict(Verdict.RTE)
                        .timeUsed(0)
                        .memoryUsed(0)
                        .errorDetails("Runtime error")
                        .build();
                return new ExecutionResult(result, "", "");
            }
        }

        JudgeResultDTO result = JudgeResultDTO.builder()
                .submissionId(job.getId())
                .verdict(Verdict.AC)
                .timeUsed(0)
                .memoryUsed(0)
                .errorDetails(null)
                .build();
        return new ExecutionResult(result, "", "");
    }

    private int runCompileCommand(
            LanguageExecutor executor,
            Path workingDirectory,
            Path sourceFilePath,
            long timeoutMs) {
        return dockerSandboxService.compile(executor, workingDirectory, sourceFilePath, timeoutMs);
    }

    private int runCommand(
            LanguageExecutor executor,
            Path workingDirectory,
            Path sourceFilePath,
            String test,
            long timeoutMs) {
        return dockerSandboxService.run(executor, workingDirectory, sourceFilePath, test, timeoutMs);
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
