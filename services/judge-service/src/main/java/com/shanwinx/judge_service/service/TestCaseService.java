package com.shanwinx.judge_service.service;

import com.shanwinx.judge_service.dto.ListTestCaseDto;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TestCaseService {

    private static final String TEST_CASE_FOLDER = "testcases";
    private static final String INPUT_FILE = "input.in";
    private static final String EXPECTED_OUTPUT_FILE = "output.expected.out";

    private final HttpClient httpClient;
    private final Duration downloadTimeout;

    public TestCaseService(@Value("${judge.testcase.download-timeout-ms:30000}") long downloadTimeoutMs) {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.downloadTimeout = Duration.ofMillis(Math.max(1, downloadTimeoutMs));
    }

    public Path prepareTestCases(UUID submissionId, Path workspacePath, List<ListTestCaseDto> testCases) {
        Path testCasesRoot = workspacePath.resolve(TEST_CASE_FOLDER);
        try {
            Files.createDirectories(testCasesRoot);
            for (ListTestCaseDto testCase : testCases) {
                prepareTestCase(submissionId, workspacePath, testCase);
            }
            return testCasesRoot;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to prepare testcases for submissionId=" + submissionId, e);
        }
    }

    public Path prepareTestCase(UUID submissionId, Path workspacePath, ListTestCaseDto testCase) {
        Path testCasesRoot = workspacePath.resolve(TEST_CASE_FOLDER);
        try {
            Files.createDirectories(testCasesRoot);
            return prepareSingleTestCase(testCasesRoot, testCase);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to prepare testcase for submissionId=" + submissionId
                    + ", testcaseId=" + (testCase == null ? null : testCase.getId()), e);
        }
    }

    private Path prepareSingleTestCase(Path testCasesRoot, ListTestCaseDto testCase) throws IOException {
        Path testCaseDir = testCasesRoot.resolve(testCase.getId().toString());
        Files.createDirectories(testCaseDir);

        downloadFile(testCase.getInputUrl(), testCaseDir.resolve(INPUT_FILE));
        downloadFile(testCase.getOutputUrl(), testCaseDir.resolve(EXPECTED_OUTPUT_FILE));
        return testCaseDir;
    }

    private void downloadFile(String url, Path outputPath) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .timeout(downloadTimeout)
                .build();

        HttpResponse<byte[]> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while downloading testcase file", e);
        }

        Files.write(outputPath, response.body(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
