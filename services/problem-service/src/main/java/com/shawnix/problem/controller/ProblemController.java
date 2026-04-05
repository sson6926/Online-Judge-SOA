package com.shawnix.problem.controller;

import com.shawnix.problem.dto.ProblemRequest;
import com.shawnix.problem.dto.TestCaseResponse;
import com.shawnix.problem.model.Problem;
import com.shawnix.problem.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<List<Problem>> getAllProblems() {
        return ResponseEntity.ok(problemService.getAllProblems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Problem> getProblemById(@PathVariable UUID id) {
        return ResponseEntity.ok(problemService.getProblemById(id));
    }

    // --- CRUD Problem ---

    @PostMapping
    public ResponseEntity<Problem> createProblem(@RequestBody ProblemRequest request) {
        return ResponseEntity.status(201).body(problemService.createProblem(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProblem(@PathVariable UUID id) {
        problemService.deleteProblem(id);
        return ResponseEntity.noContent().build();
    }

    // --- TestCase Management ---

    @PostMapping("/{id}/testcases")
    public ResponseEntity<TestCaseResponse> addTestCase(
            @PathVariable UUID id,
            @RequestParam("input") MultipartFile inputFile,
            @RequestParam("output") MultipartFile outputFile) {
        return ResponseEntity.status(201).body(problemService.addTestCase(id, inputFile, outputFile));
    }

    @DeleteMapping("/testcases/{testCaseId}")
    public ResponseEntity<Void> deleteTestCase(@PathVariable UUID testCaseId) {
        problemService.deleteTestCase(testCaseId);
        return ResponseEntity.noContent().build();
    }

    // --- Internal API for Judge Worker ---

    @GetMapping("/internal/{id}/testcases")
    public ResponseEntity<List<TestCaseResponse>> getTestCasesByProblemId(@PathVariable UUID id) {
        return ResponseEntity.ok(problemService.getTestCasesByProblemId(id));
    }
}
