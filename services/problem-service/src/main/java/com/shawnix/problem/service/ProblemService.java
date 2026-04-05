package com.shawnix.problem.service;

import com.shawnix.problem.dto.ProblemRequest;
import com.shawnix.problem.dto.TestCaseResponse;
import com.shawnix.problem.model.Problem;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProblemService {
    List<Problem> getAllProblems();
    Problem getProblemById(UUID id);

    Problem createProblem(ProblemRequest request);
    void deleteProblem(UUID id);

    TestCaseResponse addTestCase(UUID problemId, MultipartFile inputFile, MultipartFile outputFile);
    void deleteTestCase(UUID testCaseId);

    /** Internal: trả về URL đã gen cho Judge Worker */
    List<TestCaseResponse> getTestCasesByProblemId(UUID problemId);
}
