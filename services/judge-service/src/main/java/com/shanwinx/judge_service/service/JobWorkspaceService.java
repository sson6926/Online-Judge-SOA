package com.shanwinx.judge_service.service;

import com.shanwinx.judge_service.dto.SubmissionJobDTO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JobWorkspaceService {

    private final Path workspaceRoot;

    public JobWorkspaceService(@Value("${judge.workspace.root:/tmp/judge-jobs}") String workspaceRoot) {
        this.workspaceRoot = Paths.get(workspaceRoot);
    }

    public Path createWorkspace(UUID submissionId) {
        try {
            Files.createDirectories(workspaceRoot);
            return Files.createTempDirectory(workspaceRoot, submissionId + "-");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create workspace for submissionId=" + submissionId, e);
        }
    }

    public Path writeSourceFile(Path workspacePath, SubmissionJobDTO job, String sourceFileName) {
        if (workspacePath == null) {
            throw new IllegalArgumentException("workspacePath must not be null");
        }
        if (job == null || job.getId() == null) {
            throw new IllegalArgumentException("job/id must not be null");
        }

        String code = job.getCode() == null ? "" : job.getCode();
        Path sourceFilePath = workspacePath.resolve(sourceFileName);

        try {
            Files.writeString(
                    sourceFilePath,
                    code,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            return sourceFilePath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write source file for submissionId=" + job.getId(), e);
        }
    }

    public void cleanupWorkspace(Path workspacePath) {
        if (workspacePath == null) {
            return;
        }

        try (var stream = Files.walk(workspacePath)) {
            stream.sorted((left, right) -> right.compareTo(left))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new IllegalStateException("Failed to delete " + path, e);
                        }
                    });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to cleanup workspace " + workspacePath, e);
        }
    }
}
