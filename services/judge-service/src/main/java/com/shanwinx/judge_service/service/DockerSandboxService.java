package com.shanwinx.judge_service.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
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

    public ExecutionStat parseExecutionStat(Path statFilePath) {
        if (statFilePath == null || !Files.exists(statFilePath)) {
            return new ExecutionStat(0, 0);
        }

        try {
            String statRaw = Files.readString(statFilePath, StandardCharsets.UTF_8).trim();
            if (statRaw.isBlank()) {
                return new ExecutionStat(0, 0);
            }

            String[] parts = statRaw.split("\\|");
            if (parts.length < 2) {
                return new ExecutionStat(0, 0);
            }

            int timeUsed = parseSecondsToMillis(parts[0]);
            int memoryUsed = parseInteger(parts[1]);
            return new ExecutionStat(timeUsed, memoryUsed);
        } catch (IOException ex) {
            log.warn("Cannot read stat file {}", statFilePath, ex);
            return new ExecutionStat(0, 0);
        }
    }

    private int parseSecondsToMillis(String secondsText) {
        try {
            double seconds = Double.parseDouble(secondsText.trim());
            return (int) Math.max(0, Math.round(seconds * 1000));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private int parseInteger(String value) {
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException ex) {
            return 0;
        }
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

    public static final class ExecutionStat {
        private final int timeUsed;
        private final int memoryUsed;

        public ExecutionStat(int timeUsed, int memoryUsed) {
            this.timeUsed = timeUsed;
            this.memoryUsed = memoryUsed;
        }

        public int getTimeUsed() {
            return timeUsed;
        }

        public int getMemoryUsed() {
            return memoryUsed;
        }
    }
}
