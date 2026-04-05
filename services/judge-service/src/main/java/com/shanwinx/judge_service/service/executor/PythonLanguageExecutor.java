package com.shanwinx.judge_service.service.executor;

import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PythonLanguageExecutor implements LanguageExecutor {

    @Override
    public String sourceFileName() {
        return "main.py";
    }

    @Override
    public boolean requiresCompilation() {
        return false;
    }

    @Override
    public List<String> compileCommand(Path workspacePath, Path sourceFilePath) {
        return List.of();
    }

    @Override
    public List<String> runCommand(Path workspacePath, Path sourceFilePath) {
        return List.of("python3", sourceFilePath.getFileName().toString());
    }
}
