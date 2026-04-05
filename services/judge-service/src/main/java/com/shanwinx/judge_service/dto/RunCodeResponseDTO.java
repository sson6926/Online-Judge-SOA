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
public class RunCodeResponseDTO {
    private UUID submissionId;
    private Verdict verdict;
    private Integer timeUsed;
    private Integer memoryUsed;
    private String errorDetails;
    private String stdout;
    private String stderr;
}
