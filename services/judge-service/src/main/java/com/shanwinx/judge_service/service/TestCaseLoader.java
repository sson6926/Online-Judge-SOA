package com.shanwinx.judge_service.service;

import com.shanwinx.judge_service.client.ProblemServiceClient;
import com.shanwinx.judge_service.dto.ListTestCaseDto;
import com.shanwinx.judge_service.dto.ListTestCaseMockData;
import com.shanwinx.judge_service.dto.SubmissionJobDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Resolves test cases for a given job — either from mock data or problem-service.
 */
@Component
@RequiredArgsConstructor
public class TestCaseLoader {

    private static final String SOURCE_PROBLEM = "problem";

    private final ProblemServiceClient problemServiceClient;

    @Value("${judge.testcases.source:mock}")
    private String testcasesSource;

    public boolean isProblemSource() {
        return SOURCE_PROBLEM.equalsIgnoreCase(testcasesSource == null ? "" : testcasesSource.trim());
    }

    public List<ListTestCaseDto> load(SubmissionJobDTO job) {
        if (isProblemSource()) {
            List<ListTestCaseDto> result = problemServiceClient.getTestCasesByProblemId(job.getProblemId());
            return result != null ? result : List.of();
        }
        return ListTestCaseMockData.sample();
    }
}
