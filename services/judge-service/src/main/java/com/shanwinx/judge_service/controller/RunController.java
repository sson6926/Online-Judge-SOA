package com.shanwinx.judge_service.controller;

import com.shanwinx.judge_service.dto.JudgeResultDTO;
import com.shanwinx.judge_service.dto.RunCodeRequestDTO;
import com.shanwinx.judge_service.dto.RunCodeResponseDTO;
import com.shanwinx.judge_service.dto.SubmissionJobDTO;
import com.shanwinx.judge_service.dto.Verdict;
import com.shanwinx.judge_service.service.JudgeExecutionService;
import com.shanwinx.judge_service.service.JudgeExecutionService.ExecutionResult;
import com.shanwinx.judge_service.service.JobWorkspaceService;
import java.nio.file.Path;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class RunController {

    private final JudgeExecutionService judgeExecutionService;
    private final JobWorkspaceService jobWorkspaceService;

    @PostMapping("/run")
    public ResponseEntity<RunCodeResponseDTO> run(@RequestBody RunCodeRequestDTO request) {
        if (request == null || isBlank(request.getLanguage()) || isBlank(request.getCode())) {
            return ResponseEntity.badRequest().body(RunCodeResponseDTO.builder()
                    .submissionId(null)
                    .verdict(Verdict.SE)
                    .timeUsed(0)
                    .memoryUsed(0)
                    .errorDetails("language and code are required")
                    .stdout("")
                    .stderr("language and code are required")
                    .build());
        }

        UUID submissionId = UUID.randomUUID();
        SubmissionJobDTO job = SubmissionJobDTO.builder()
                .id(submissionId)
                .problemId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .language(request.getLanguage())
                .code(request.getCode())
                .build();

        Path workspacePath = null;
        try {
            workspacePath = jobWorkspaceService.createWorkspace(submissionId);
            ExecutionResult executionResult = judgeExecutionService.executeDetailed(job, workspacePath);
            return ResponseEntity.ok(judgeExecutionService.toRunCodeResponse(executionResult));
        } catch (RuntimeException ex) {
            log.error("Run API failed for submissionId={}", submissionId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RunCodeResponseDTO.builder()
                    .submissionId(submissionId)
                    .verdict(Verdict.SE)
                    .timeUsed(0)
                    .memoryUsed(0)
                    .errorDetails("Run API failed: " + ex.getMessage())
                    .stdout("")
                    .stderr(ex.getMessage())
                    .build());
        } finally {
            try {
                jobWorkspaceService.cleanupWorkspace(workspacePath);
            } catch (RuntimeException cleanupEx) {
                log.warn("Cannot cleanup workspace for submissionId={} workspace={}", submissionId, workspacePath, cleanupEx);
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
