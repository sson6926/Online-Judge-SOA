package com.shawnix.problem.dto;

import com.shawnix.problem.model.Problem;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TestCaseResponse {
    private UUID id;
    private UUID problemId;
    private String inputUrl;    // presigned URL generated on-the-fly
    private String outputUrl;

    // Problem metadata — useful for Judge Service
    private String problemTitle;
    private Integer timeLimit;   // ms
    private Integer memoryLimit; // KB
    private Problem.Difficulty difficulty;
}
