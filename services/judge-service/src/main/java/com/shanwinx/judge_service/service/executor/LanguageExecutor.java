package com.shanwinx.judge_service.service.executor;

import java.nio.file.Path;
import java.util.List;

public interface LanguageExecutor {

    String sourceFileName();

    boolean requiresCompilation();

    List<String> compileCommand(Path workspacePath, Path sourceFilePath);

    List<String> runCommand(Path workspacePath, Path sourceFilePath);
}
