package com.shawnix.problem.service;

import com.shawnix.problem.dto.ProblemRequest;
import com.shawnix.problem.dto.TestCaseResponse;
import com.shawnix.problem.model.Problem;
import com.shawnix.problem.model.TestCase;
import com.shawnix.problem.repository.ProblemRepository;
import com.shawnix.problem.repository.TestCaseRepository;
import com.shawnix.problem.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final MinioService minioService;

    @Override
    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    @Override
    public Problem getProblemById(UUID id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found: " + id));
    }

    @Override
    public Problem createProblem(ProblemRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank())
            throw new IllegalArgumentException("title is required");
        if (request.getDifficulty() == null)
            throw new IllegalArgumentException("difficulty is required");

        Problem problem = Problem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .timeLimit(request.getTimeLimit())
                .memoryLimit(request.getMemoryLimit())
                .difficulty(request.getDifficulty())
                .build();
        return problemRepository.save(problem);
    }

    @Override
    @Transactional
    public void deleteProblem(UUID id) {
        if (!problemRepository.existsById(id))
            throw new ResourceNotFoundException("Problem not found: " + id);
        // Xóa tất cả testcase files trên MinIO trước
        List<TestCase> testCases = testCaseRepository.findByProblemId(id);
        for (TestCase tc : testCases) {
            if (tc.getInputKey() != null) minioService.delete(tc.getInputKey());
            if (tc.getOutputKey() != null) minioService.delete(tc.getOutputKey());
        }
        testCaseRepository.deleteAll(testCases);
        problemRepository.deleteById(id);
    }

    @Override
    public TestCaseResponse addTestCase(UUID problemId, MultipartFile inputFile, MultipartFile outputFile) {
        if (inputFile == null || inputFile.isEmpty())
            throw new IllegalArgumentException("input file is required");
        if (outputFile == null || outputFile.isEmpty())
            throw new IllegalArgumentException("output file is required");

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found: " + problemId));

        String inputKey = "problems/" + problemId + "/in_" + UUID.randomUUID() + ".txt";
        String outputKey = "problems/" + problemId + "/out_" + UUID.randomUUID() + ".txt";

        minioService.upload(inputKey, inputFile);
        minioService.upload(outputKey, outputFile);

        TestCase tc = TestCase.builder()
                .problem(problem)
                .inputKey(inputKey)
                .outputKey(outputKey)
                .build();
        tc = testCaseRepository.save(tc);

        return toResponse(tc);
    }

    @Override
    @Transactional
    public void deleteTestCase(UUID testCaseId) {
        TestCase tc = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("TestCase not found: " + testCaseId));
        if (tc.getInputKey() != null) minioService.delete(tc.getInputKey());
        if (tc.getOutputKey() != null) minioService.delete(tc.getOutputKey());
        testCaseRepository.delete(tc);
    }

    @Override
    public List<TestCaseResponse> getTestCasesByProblemId(UUID problemId) {
        return testCaseRepository.findByProblemId(problemId).stream()
                .map(this::toResponse)
                .toList();
    }

    private TestCaseResponse toResponse(TestCase tc) {
        return TestCaseResponse.builder()
                .id(tc.getId())
                .problemId(tc.getProblem().getId())
                .inputUrl(tc.getInputKey() != null ? minioService.generateUrl(tc.getInputKey()) : null)
                .outputUrl(tc.getOutputKey() != null ? minioService.generateUrl(tc.getOutputKey()) : null)
                .build();
    }
}
