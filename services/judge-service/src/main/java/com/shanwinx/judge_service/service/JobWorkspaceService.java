package com.shanwinx.judge_service.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
