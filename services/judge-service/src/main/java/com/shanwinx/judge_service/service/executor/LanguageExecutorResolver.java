package com.shanwinx.judge_service.service.executor;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LanguageExecutorResolver {

    private final JavaLanguageExecutor javaLanguageExecutor;
    private final PythonLanguageExecutor pythonLanguageExecutor;
    private final CppLanguageExecutor cppLanguageExecutor;

    public LanguageExecutor resolve(String language) {
        if (language == null) {
            return null;
        }

        String normalized = language.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "java" -> javaLanguageExecutor;
            case "python", "py" -> pythonLanguageExecutor;
            case "cpp", "c++" -> cppLanguageExecutor;
            default -> null;
        };
    }
}
