package com.shanwinx.judge_service.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListTestCaseDto {
    private UUID id;
    private UUID problemId;
    private String inputUrl;
    private String outputUrl;
    private String problemTitle;
    private Integer timeLimit;
    private Integer memoryLimit;
    private String difficulty;
}
