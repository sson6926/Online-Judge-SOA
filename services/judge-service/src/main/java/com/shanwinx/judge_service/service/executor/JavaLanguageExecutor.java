package com.shanwinx.judge_service.service.executor;

import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JavaLanguageExecutor implements LanguageExecutor {

    @Override
    public String sourceFileName() {
        return "Main.java";
    }

    @Override
    public boolean requiresCompilation() {
        return true;
    }

    @Override
    public List<String> compileCommand(Path workspacePath, Path sourceFilePath) {
        return List.of("javac", sourceFilePath.getFileName().toString());
    }

    @Override
    public List<String> runCommand(Path workspacePath, Path sourceFilePath) {
        return List.of("java", "Main");
    }
}
