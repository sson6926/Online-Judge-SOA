package com.shanwinx.judge_service.service;

import com.shanwinx.judge_service.service.executor.LanguageExecutor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DockerSandboxService {

    private static final String STAT_SEPARATOR = "|";
    private static final String COMPILE_LOG_FILE = "compile.log";
    private static final String COMPILE_ERROR_FILE = "compile.err";
    private static final String RUN_STAT_FILE = "stat";
    private static final String INPUT_FILE = "input.in";
    private static final String USER_OUTPUT_FILE = "output.user.out";
    private static final String RUN_ERROR_FILE = "error.log";

    @Value("${judge.sandbox.image:judge-sandbox:latest}")
    private String sandboxImage;

    @Value("${judge.workspace.root:/tmp/judge-jobs}")
    private String containerWorkspaceRoot;

    @Value("${judge.workspace.host-root:}")
    private String hostWorkspaceRoot;

    /**
     * Returns the host-side path for a given container workspace path.
     * Needed because docker run -v requires host paths, not container-internal paths.
     */
    private String toHostPath(String containerPath) {
        if (hostWorkspaceRoot == null || hostWorkspaceRoot.isBlank()) {
            return containerPath; // running on host directly, paths are the same
        }
        return containerPath.replace(containerWorkspaceRoot, hostWorkspaceRoot);
    }

    public int compile(LanguageExecutor executor, Path workingDirectory, Path sourceFilePath, long timeoutMs) {
        List<String> command = executor == null ? null : executor.compileCommand(workingDirectory, sourceFilePath);
        if (command == null || command.isEmpty()) {
            return 0;
        }

        String workspacePath = workingDirectory.toAbsolutePath().normalize().toString();
        String shellCommand = buildShellCommand(command) + " && chmod +x main 2>/dev/null || true";
        List<String> dockerCommand = new ArrayList<>();
        dockerCommand.add("docker");
        dockerCommand.add("run");
        dockerCommand.add("--rm");
        dockerCommand.add("-v");
        dockerCommand.add(toHostPath(workspacePath) + ":/workspace");
        dockerCommand.add("-w");
        dockerCommand.add("/workspace");
        dockerCommand.add("--entrypoint");
        dockerCommand.add("/bin/bash");
        dockerCommand.add(sandboxImage);
        dockerCommand.add("-lc");
        dockerCommand.add(shellCommand);

        Path compileLog = workingDirectory.resolve(COMPILE_LOG_FILE);
        Path compileErr = workingDirectory.resolve(COMPILE_ERROR_FILE);

        try {
            Files.writeString(compileLog, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(compileErr, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create compile log files", e);
        }

        return executeDockerCommand(dockerCommand, timeoutMs, compileLog, compileErr);
    }

    public int run(LanguageExecutor executor, Path workingDirectory, Path sourceFilePath, String test, long timeoutMs) {
        List<String> command = executor == null ? null : executor.runCommand(workingDirectory, sourceFilePath);
        if (command == null || command.isEmpty()) {
            return 0;
        }

        Path testDirectory = workingDirectory.resolve("testcases").resolve(test == null || test.isBlank() ? "default" : test);
        Path inputFilePath = testDirectory.resolve(INPUT_FILE);
        Path userOutputFilePath = testDirectory.resolve(USER_OUTPUT_FILE);
        Path statFilePath = testDirectory.resolve(RUN_STAT_FILE);
        Path runErrFilePath = testDirectory.resolve(RUN_ERROR_FILE);

        try {
            Files.createDirectories(testDirectory);
            Files.writeString(userOutputFilePath, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(runErrFilePath, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(statFilePath, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create run artifacts in " + testDirectory, e);
        }

        String workspacePath = workingDirectory.toAbsolutePath().normalize().toString();
        String shellCommand = buildTimedShellCommandWithRedirect(
                command,
                toContainerPath(workingDirectory, statFilePath),
                toContainerPath(workingDirectory, inputFilePath),
                toContainerPath(workingDirectory, userOutputFilePath),
                toContainerPath(workingDirectory, runErrFilePath));

        List<String> dockerCommand = new ArrayList<>();
        dockerCommand.add("docker");
        dockerCommand.add("run");
        dockerCommand.add("-i");
        dockerCommand.add("--rm");
        dockerCommand.add("-v");
        dockerCommand.add(toHostPath(workspacePath) + ":/workspace");
        dockerCommand.add("-w");
        dockerCommand.add("/workspace");
        dockerCommand.add("--entrypoint");
        dockerCommand.add("/bin/bash");
        dockerCommand.add(sandboxImage);
        dockerCommand.add("-lc");
        dockerCommand.add(shellCommand);

        return executeDockerCommand(dockerCommand, timeoutMs, null, runErrFilePath);
    }

    private int executeDockerCommand(List<String> dockerCommand, long timeoutMs, Path outputFile, Path errorFile) {
        try {
            ProcessBuilder builder = new ProcessBuilder(dockerCommand);
            if (outputFile != null) {
                builder.redirectOutput(outputFile.toFile());
            }
            if (errorFile != null) {
                builder.redirectError(errorFile.toFile());
            }

            Process process = builder.start();
            boolean completed = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);

            if (!completed) {
                process.destroyForcibly();
                return -1;
            }

            return process.exitValue();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot execute command in sandbox: " + String.join(" ", dockerCommand), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Command interrupted: " + String.join(" ", dockerCommand), e);
        }
    }

    private String toContainerPath(Path workingDirectory, Path statFilePath) {
        Path absoluteWorkspace = workingDirectory.toAbsolutePath().normalize();
        Path absoluteStat = statFilePath.toAbsolutePath().normalize();
        Path relative = absoluteWorkspace.relativize(absoluteStat);
        return "/workspace/" + relative.toString().replace('\\', '/');
    }

    private String buildTimedShellCommandWithRedirect(
            List<String> command,
            String statFilePath,
            String inputFilePath,
            String outputFilePath,
            String errorFilePath) {
        return "/usr/bin/time -f '%e" + STAT_SEPARATOR + "%M' -o "
                + shellEscape(statFilePath)
                + " -- "
                + buildShellCommand(command)
                + " < "
                + shellEscape(inputFilePath)
                + " > "
                + shellEscape(outputFilePath)
                + " 2> "
                + shellEscape(errorFilePath);
    }

    private String buildShellCommand(List<String> command) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < command.size(); i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(shellEscape(command.get(i)));
        }
        return builder.toString();
    }

    private String shellEscape(String value) {
        if (value == null) {
            return "''";
        }
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }
}
