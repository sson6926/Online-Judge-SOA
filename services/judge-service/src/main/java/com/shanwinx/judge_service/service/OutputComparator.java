package com.shanwinx.judge_service.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

/**
 * Compares user output against expected output with whitespace normalization.
 */
@Component
public class OutputComparator {

    public boolean matches(Path userOut, Path expectedOut) throws IOException {
        String actual = Files.readString(userOut);
        String expected = Files.readString(expectedOut);
        return normalize(actual).equals(normalize(expected));
    }

    // package-private for testing
    static String normalize(String raw) {
        if (raw == null) return "";
        return raw.replace("\r\n", "\n").replace('\r', '\n').stripTrailing();
    }
}
