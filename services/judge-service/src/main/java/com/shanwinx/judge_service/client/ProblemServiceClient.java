package com.shanwinx.judge_service.client;

import com.shanwinx.judge_service.dto.ListTestCaseDto;
import com.shanwinx.judge_service.exception.RetryableJudgeException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "problem-service")
public interface ProblemServiceClient {

    @CircuitBreaker(name = "problemServiceClient", fallbackMethod = "getTestCasesByProblemIdFallback")
    @GetMapping("/problems/internal/{id}/testcases")
    List<ListTestCaseDto> getTestCasesByProblemId(@PathVariable("id") UUID problemId);

    default List<ListTestCaseDto> getTestCasesByProblemIdFallback(UUID problemId, Throwable throwable) {
        throw new RetryableJudgeException(
                "Problem-service unavailable or circuit open while fetching testcases for problemId=" + problemId,
                throwable);
    }
}
