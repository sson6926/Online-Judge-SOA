package com.shanwinx.judge_service.service.executor;

import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CppLanguageExecutor implements LanguageExecutor {

    @Override
    public String sourceFileName() {
        return "main.cpp";
    }

    @Override
    public boolean requiresCompilation() {
        return true;
    }

    @Override
    public List<String> compileCommand(Path workspacePath, Path sourceFilePath) {
        return List.of(
                "g++",
                sourceFilePath.getFileName().toString(),
                "-O2",
                "-std=c++17",
                "-o",
                "main");
    }

    @Override
    public List<String> runCommand(Path workspacePath, Path sourceFilePath) {
        return List.of("./main");
    }
}
