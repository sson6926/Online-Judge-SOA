package com.shanwinx.judge_service.service;

import com.shanwinx.judge_service.client.ProblemServiceClient;
import com.shanwinx.judge_service.dto.ListTestCaseDto;
import com.shanwinx.judge_service.dto.SubmissionJobDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves test cases for a given job from problem-service.
 */
@Component
@RequiredArgsConstructor
public class TestCaseLoader {

    private final ProblemServiceClient problemServiceClient;

    public List<ListTestCaseDto> load(SubmissionJobDTO job) {
        List<ListTestCaseDto> result = problemServiceClient.getTestCasesByProblemId(job.getProblemId());
        return result != null ? result : List.of();
    }
}
