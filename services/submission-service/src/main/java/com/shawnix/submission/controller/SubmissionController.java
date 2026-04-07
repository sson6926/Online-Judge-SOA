package com.shawnix.submission.controller;

import com.shawnix.submission.dto.SubmitRequest;
import com.shawnix.submission.model.Submission;
import com.shawnix.submission.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<Submission> submit(
            @Valid @RequestBody SubmitRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId) {

        if (xUserId != null && !xUserId.isBlank()) {
            request.setUserId(UUID.fromString(xUserId));
        }

        Submission result = submissionService.submit(request);
        return ResponseEntity.accepted().body(result);
    }

    @GetMapping
    public ResponseEntity<List<Submission>> getAllSubmissions() {
        return ResponseEntity.ok(submissionService.getAll());
    }

    @GetMapping("/problem/{problemId}")
    public ResponseEntity<List<Submission>> getByProblemId(@PathVariable UUID problemId) {
        return ResponseEntity.ok(submissionService.getByProblemId(problemId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Submission> getSubmissionById(@PathVariable UUID id) {
        Submission submission = submissionService.getSubmissionById(id);
        if (submission == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(submission);
    }
}
