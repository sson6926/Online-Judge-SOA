package com.shawnix.problem.service;

import com.shawnix.problem.model.Problem;
import com.shawnix.problem.model.TestCase;
import java.util.List;
import java.util.UUID;

public interface ProblemService {
    List<Problem> getAllProblems();
    Problem getProblemById(UUID id);
    List<TestCase> getTestCasesByProblemId(UUID problemId);
}
