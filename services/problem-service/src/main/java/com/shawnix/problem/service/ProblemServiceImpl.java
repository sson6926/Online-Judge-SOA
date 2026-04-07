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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final MinioService minioService;

    @Override
    public List<Problem> getAllProblems() {
        List<Problem> problems = problemRepository.findAll();
        log.info("Lấy danh sách problems: {} bài", problems.size());
        return problems;
    }

    @Override
    public Problem getProblemById(UUID id) {
        log.info("Lấy problem ID: {}", id);
        return problemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy problem ID: {}", id);
                    return new ResourceNotFoundException("Problem not found: " + id);
                });
    }

    @Override
    public Problem createProblem(ProblemRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank())
            throw new IllegalArgumentException("title is required");
        if (request.getDifficulty() == null)
            throw new IllegalArgumentException("difficulty is required");

        log.info("Tạo problem mới: title='{}', difficulty={}", request.getTitle(), request.getDifficulty());
        Problem problem = Problem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .timeLimit(request.getTimeLimit())
                .memoryLimit(request.getMemoryLimit())
                .difficulty(request.getDifficulty())
                .build();
        Problem saved = problemRepository.save(problem);
        log.info("Tạo problem thành công: ID={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void deleteProblem(UUID id) {
        if (!problemRepository.existsById(id)) {
            log.warn("Xóa thất bại - không tìm thấy problem ID: {}", id);
            throw new ResourceNotFoundException("Problem not found: " + id);
        }
        List<TestCase> testCases = testCaseRepository.findByProblemId(id);
        log.info("Xóa problem ID: {} kèm {} testcase(s)", id, testCases.size());
        for (TestCase tc : testCases) {
            if (tc.getInputKey() != null) {
                minioService.delete(tc.getInputKey());
                log.debug("Đã xóa file MinIO: {}", tc.getInputKey());
            }
            if (tc.getOutputKey() != null) {
                minioService.delete(tc.getOutputKey());
                log.debug("Đã xóa file MinIO: {}", tc.getOutputKey());
            }
        }
        testCaseRepository.deleteAll(testCases);
        problemRepository.deleteById(id);
        log.info("Xóa problem ID: {} thành công", id);
    }

    @Override
    public TestCaseResponse addTestCase(UUID problemId, MultipartFile inputFile, MultipartFile outputFile) {
        if (inputFile == null || inputFile.isEmpty())
            throw new IllegalArgumentException("input file is required");
        if (outputFile == null || outputFile.isEmpty())
            throw new IllegalArgumentException("output file is required");

        log.info("Thêm testcase cho problem ID: {}", problemId);
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy problem ID: {} khi thêm testcase", problemId);
                    return new ResourceNotFoundException("Problem not found: " + problemId);
                });

        String inputKey = "problems/" + problemId + "/in_" + UUID.randomUUID() + ".txt";
        String outputKey = "problems/" + problemId + "/out_" + UUID.randomUUID() + ".txt";

        log.debug("Upload input → MinIO key: {}", inputKey);
        minioService.upload(inputKey, inputFile);
        log.debug("Upload output → MinIO key: {}", outputKey);
        minioService.upload(outputKey, outputFile);

        TestCase tc = TestCase.builder()
                .problem(problem)
                .inputKey(inputKey)
                .outputKey(outputKey)
                .build();
        tc = testCaseRepository.save(tc);
        log.info("Thêm testcase thành công: ID={} cho problem ID={}", tc.getId(), problemId);
        return toResponse(tc);
    }

    @Override
    @Transactional
    public void deleteTestCase(UUID testCaseId) {
        log.info("Xóa testcase ID: {}", testCaseId);
        TestCase tc = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy testcase ID: {}", testCaseId);
                    return new ResourceNotFoundException("TestCase not found: " + testCaseId);
                });
        if (tc.getInputKey() != null) minioService.delete(tc.getInputKey());
        if (tc.getOutputKey() != null) minioService.delete(tc.getOutputKey());
        testCaseRepository.delete(tc);
        log.info("Xóa testcase ID: {} thành công", testCaseId);
    }

    @Override
    public List<TestCaseResponse> getTestCasesByProblemId(UUID problemId) {
        List<TestCase> testCases = testCaseRepository.findByProblemId(problemId);
        log.info("[INTERNAL] Judge lấy {} testcase(s) cho problem ID: {}", testCases.size(), problemId);
        return testCases.stream().map(this::toResponse).toList();
    }

    private TestCaseResponse toResponse(TestCase tc) {
        Problem p = tc.getProblem();
        return TestCaseResponse.builder()
                .id(tc.getId())
                .problemId(p.getId())
                .inputUrl(tc.getInputKey() != null ? minioService.generateUrl(tc.getInputKey()) : null)
                .outputUrl(tc.getOutputKey() != null ? minioService.generateUrl(tc.getOutputKey()) : null)
                .problemTitle(p.getTitle())
                .timeLimit(p.getTimeLimit())
                .memoryLimit(p.getMemoryLimit())
                .difficulty(p.getDifficulty())
                .build();
    }
}
