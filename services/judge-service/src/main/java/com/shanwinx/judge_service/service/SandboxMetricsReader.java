package com.shanwinx.judge_service.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

/**
 * Reads and parses the stat file written by {@code /usr/bin/time} inside the sandbox.
 * Format: {@code <elapsed_seconds>|<max_rss_kb>}
 */
@Component
public class SandboxMetricsReader {

    public record Metrics(double elapsedSeconds, long maxRssKb) {
        public int elapsedMs() {
            return (int) Math.round(elapsedSeconds * 1000.0);
        }
    }

    /**
     * @return parsed metrics, or {@code null} if the file is missing/malformed.
     */
    public Metrics read(Path statFile) {
        try {
            if (!Files.isRegularFile(statFile)) return null;
            String content = Files.readString(statFile).trim();
            if (content.isEmpty()) return null;

            int sep = content.indexOf('|');
            if (sep < 0) return null;

            double elapsed = Double.parseDouble(content.substring(0, sep).trim());
            long rssKb = Long.parseLong(content.substring(sep + 1).trim());
            return new Metrics(elapsed, rssKb);
        } catch (IOException | NumberFormatException e) {
            return null;
        }
    }
}
