package com.shawnix.submission.controller;

import com.shawnix.submission.model.Submission;
import com.shawnix.submission.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.validation.BindingResult;

@RestController
@RequestMapping("/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<?> submit(@Valid @RequestBody Submission submission,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                    e -> e.getField(),
                    e -> e.getDefaultMessage()
                ));
            return ResponseEntity.badRequest().body(errors);
        }
        Submission result = submissionService.submit(submission);
        return ResponseEntity.accepted().body(result);
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
