package com.shawnix.problem.dto;

import com.shawnix.problem.model.Problem;
import lombok.Data;

@Data
public class ProblemRequest {
    private String title;
    private String description;
    private Integer timeLimit;
    private Integer memoryLimit;
    private Problem.Difficulty difficulty;
}
