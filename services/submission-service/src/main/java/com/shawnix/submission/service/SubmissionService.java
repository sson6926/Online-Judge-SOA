package com.shawnix.submission.service;

import com.shawnix.submission.model.Submission;
import java.util.UUID;

public interface SubmissionService {
    Submission submit(Submission submission);
    Submission getSubmissionById(UUID id);
}
