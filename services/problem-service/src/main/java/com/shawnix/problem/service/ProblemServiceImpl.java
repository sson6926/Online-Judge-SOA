package com.shawnix.problem.service;

import com.shawnix.problem.model.Problem;
import com.shawnix.problem.model.TestCase;
import com.shawnix.problem.repository.ProblemRepository;
import com.shawnix.problem.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    @Override
    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    @Override
    public Problem getProblemById(UUID id) {
        return problemRepository.findById(id).orElse(null);
    }

    @Override
    public List<TestCase> getTestCasesByProblemId(UUID problemId) {
        return testCaseRepository.findByProblemId(problemId);
    }
}
