package com.shanwinx.judge_service.client;

import com.shanwinx.judge_service.dto.ListTestCaseDto;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "problem-service", url = "${services.problem-service.url:http://problem-service:5001}")
public interface ProblemServiceClient {

    @GetMapping("/problems/internal/{id}/testcases")
    List<ListTestCaseDto> getTestCasesByProblemId(@PathVariable("id") UUID problemId);
}
