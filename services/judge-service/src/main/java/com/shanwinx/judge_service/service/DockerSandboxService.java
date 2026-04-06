package com.shanwinx.judge_service.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DockerSandboxService {

    private static final String STAT_SEPARATOR = "|";

    @Value("${judge.sandbox.image:judge-sandbox:latest}")
    private String sandboxImage;

    public ProcessOutcome executeCommand(List<String> command, Path workingDirectory, long timeoutMs, String phase) {
        return new ProcessOutcome(0, "", "", false, 0, 0);
    }

    public List<String> buildDockerCommand(List<String> command, Path workingDirectory, String statFileName) {
        String workspacePath = workingDirectory.toAbsolutePath().normalize().toString();
        String shellCommand = buildTimedShellCommand(command, "/workspace/" + statFileName);
        List<String> dockerCommand = new ArrayList<>();
        dockerCommand.add("docker");
        dockerCommand.add("run");
        dockerCommand.add("--rm");
        dockerCommand.add("--network");
        dockerCommand.add("none");
        dockerCommand.add("-v");
        dockerCommand.add(workspacePath + ":/workspace");
        dockerCommand.add("-w");
        dockerCommand.add("/workspace");
        dockerCommand.add("--entrypoint");
        dockerCommand.add("/bin/bash");
        dockerCommand.add(sandboxImage);
        dockerCommand.add("-lc");
        dockerCommand.add(shellCommand);
        return dockerCommand;
    }

    private String buildTimedShellCommand(List<String> command, String statFilePath) {
        return "/usr/bin/time -f '%e" + STAT_SEPARATOR + "%M' -o "
                + shellEscape(statFilePath)
                + " -- "
                + buildShellCommand(command);
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

    public static final class ProcessOutcome {
        private final int exitCode;
        private final String stdout;
        private final String stderr;
        private final boolean timedOut;
        private final int timeUsed;
        private final int memoryUsed;

        public ProcessOutcome(int exitCode, String stdout, String stderr, boolean timedOut, int timeUsed, int memoryUsed) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
            this.timedOut = timedOut;
            this.timeUsed = timeUsed;
            this.memoryUsed = memoryUsed;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public boolean isTimedOut() {
            return timedOut;
        }

        public int getTimeUsed() {
            return timeUsed;
        }

        public int getMemoryUsed() {
            return memoryUsed;
        }
    }
}
