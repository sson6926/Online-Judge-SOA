package com.shawnix.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SubmitRequest {

    @NotNull(message = "problem_id không được để trống")
    private UUID problemId;

    @NotNull(message = "user_id không được để trống")
    private UUID userId;

    @NotBlank(message = "code không được để trống")
    private String code;

    @NotBlank(message = "language không được để trống")
    private String language;
}
