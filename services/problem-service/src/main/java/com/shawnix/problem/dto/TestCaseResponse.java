package com.shawnix.problem.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TestCaseResponse {
    private UUID id;
    private UUID problemId;
    private String inputUrl;   // presigned URL generated on-the-fly
    private String outputUrl;
}
