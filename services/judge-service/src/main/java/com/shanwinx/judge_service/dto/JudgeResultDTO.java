package com.shanwinx.judge_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeResultDTO {
    private UUID submissionId;
    private Verdict verdict;
    private Integer timeUsed;
    private Integer memoryUsed;
    private String errorDetails;
}
