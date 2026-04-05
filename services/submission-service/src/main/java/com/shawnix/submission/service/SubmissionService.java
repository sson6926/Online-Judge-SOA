package com.shawnix.submission.service;

import com.shawnix.submission.dto.SubmitRequest;
import com.shawnix.submission.model.Submission;
import java.util.UUID;

public interface SubmissionService {
    Submission submit(SubmitRequest request);
    Submission getSubmissionById(UUID id);
}
