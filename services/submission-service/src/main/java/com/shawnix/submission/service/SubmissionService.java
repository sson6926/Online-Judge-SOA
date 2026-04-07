package com.shawnix.submission.service;

import com.shawnix.submission.dto.SubmitRequest;
import com.shawnix.submission.model.Submission;
import java.util.List;
import java.util.UUID;

public interface SubmissionService {
    Submission submit(SubmitRequest request);
    Submission getSubmissionById(UUID id);
    List<Submission> getAll();
    List<Submission> getByProblemId(UUID problemId);
}
