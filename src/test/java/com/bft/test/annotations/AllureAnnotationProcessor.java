package com.bft.test.annotations;

import io.qameta.allure.Allure;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Обработчик кастомных аннотаций для интеграции с Allure
 * Автоматически добавляет информацию из аннотаций в Allure отчеты
 */
public class AllureAnnotationProcessor implements BeforeEachCallback, AfterTestExecutionCallback {

    private static final Logger logger = LoggerFactory.getLogger(AllureAnnotationProcessor.class);

    private final Map<String, Long> testStartTimes = new ConcurrentHashMap<>();

    @Override
    public void beforeEach(ExtensionContext context) {
        Optional<Method> testMethod = context.getTestMethod();
        if (testMethod.isEmpty()) {
            return;
        }

        try {
            processTestAnnotations(testMethod.get(), context.getRequiredTestInstance());
            testStartTimes.put(getTestId(context), System.currentTimeMillis());
        } catch (Exception e) {
            logger.warn("Ошибка обработки аннотаций для теста: {}", e.getMessage());
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        try {
            attachExecutionMetadata(context);
        } finally {
            testStartTimes.remove(getTestId(context));
        }
    }

    /**
     * Обработка всех кастомных аннотаций теста
     */
    private void processTestAnnotations(Method method, Object testInstance) {
        Class<?> testClass = testInstance.getClass();

        processClassAnnotations(testClass);
        processMethodAnnotations(method);
        createComprehensiveDescription(method, testClass);
    }

    /**
     * Обработка аннотаций класса
     */
    private void processClassAnnotations(Class<?> testClass) {
        Optional.ofNullable(testClass.getAnnotation(TestType.class))
            .ifPresent(annotation -> {
                Allure.epic(annotation.value().getDisplayName());
                if (!annotation.description().isEmpty()) {
                    Allure.description("Тип теста: " + annotation.description());
                }
            });

        Optional.ofNullable(testClass.getAnnotation(TestPriority.class))
            .ifPresent(annotation -> {
                SeverityLevel severity = mapPriorityToSeverity(annotation.value());
                Allure.label("severity", severity.name().toLowerCase());
            });

        Optional.ofNullable(testClass.getAnnotation(Requirement.class))
            .ifPresent(annotation -> {
                Allure.label("requirement", annotation.id());
                Allure.label("requirement.name", annotation.name());
                Allure.label("requirement.source", annotation.source());
                Allure.label("requirement.version", annotation.version());
                Allure.label("requirement.status", annotation.status().name());
            });

        Optional.ofNullable(testClass.getAnnotation(TestEnvironment.class))
            .ifPresent(annotation -> {
                String envDescription = buildEnvironmentDescription(annotation);
                Allure.label("environment", envDescription);
            });
    }

    /**
     * Обработка аннотаций метода
     */
    private void processMethodAnnotations(Method method) {
        Optional.ofNullable(method.getAnnotation(TestPriority.class))
            .ifPresent(annotation -> {
                SeverityLevel severity = mapPriorityToSeverity(annotation.value());
                Allure.label("severity", severity.name().toLowerCase());

                if (!annotation.reason().isEmpty()) {
                    Allure.description("Причина приоритета: " + annotation.reason());
                }
            });

        Optional.ofNullable(method.getAnnotation(Requirement.class))
            .ifPresent(annotation -> {
                Allure.label("requirement", annotation.id());
                Allure.label("requirement.name", annotation.name());
                Allure.label("requirement.version", annotation.version());
                Allure.label("requirement.status", annotation.status().name());
            });

        Arrays.stream(method.getAnnotationsByType(AutomationAction.class))
            .forEach(annotation -> {
                Allure.label("automation.action", annotation.value());
                Allure.label("automation.type", annotation.type().name());
                Allure.label("automation.timeout", String.valueOf(annotation.timeout()));

                if (annotation.takeScreenshot()) {
                    Allure.label("automation.screenshot", "true");
                }
            });
    }

    /**
     * Создание комплексного описания теста
     */
    private void createComprehensiveDescription(Method method, Class<?> testClass) {
        StringBuilder description = new StringBuilder();

        Optional.ofNullable(testClass.getAnnotation(TestType.class))
            .ifPresent(annotation -> {
                description.append("**Тип теста:** ").append(annotation.value().getDisplayName());
                if (!annotation.description().isEmpty()) {
                    description.append(" - ").append(annotation.description());
                }
                description.append("\n\n");
            });

        Optional.ofNullable(method.getAnnotation(TestPriority.class))
            .ifPresent(annotation -> {
                description.append("**Приоритет:** ").append(annotation.value().getDescription());
                if (!annotation.reason().isEmpty()) {
                    description.append(" (").append(annotation.reason()).append(")");
                }
                description.append("\n\n");
            });

        Optional.ofNullable(method.getAnnotation(Requirement.class))
            .ifPresent(annotation -> {
                description.append("**Требование:** ").append(annotation.name())
                          .append(" (ID: ").append(annotation.id()).append(")\n");
                description.append("**Источник:** ").append(annotation.source()).append("\n");
                description.append("**Версия:** ").append(annotation.version()).append("\n");
                description.append("**Статус:** ").append(annotation.status().name()).append("\n\n");
            });

        Optional.ofNullable(testClass.getAnnotation(TestEnvironment.class))
            .ifPresent(annotation -> {
                description.append("**Требования к окружению:**\n");
                if (annotation.browsers().length > 0) {
                    description.append("- Браузеры: ").append(Arrays.toString(annotation.browsers())).append("\n");
                }
                if (annotation.operatingSystems().length > 0) {
                    description.append("- ОС: ").append(Arrays.toString(annotation.operatingSystems())).append("\n");
                }
                if (!annotation.minJavaVersion().isEmpty()) {
                    description.append("- Java: ").append(annotation.minJavaVersion()).append("+\n");
                }
                description.append("\n");
            });

        if (description.length() > 0) {
            Allure.description(description.toString());
        }
    }

    /**
     * Добавление метаданных выполнения
     */
    private void attachExecutionMetadata(ExtensionContext context) {
        try {
            String testId = getTestId(context);
            Long startTime = testStartTimes.get(testId);
            long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
            Allure.label("execution.duration", String.valueOf(duration));

            context.getExecutionException().ifPresent(throwable -> {
                Allure.label("execution.status", "failed");
                Allure.label("execution.error", throwable.getMessage() != null
                    ? throwable.getMessage() : throwable.getClass().getSimpleName());
            });

            if (context.getExecutionException().isEmpty()) {
                Allure.label("execution.status", "passed");
            }

            context.getTestMethod().ifPresent(method -> {
                Allure.label("method.signature", method.toString());
                Allure.label("class.name", method.getDeclaringClass().getName());
            });

        } catch (Exception e) {
            logger.debug("Ошибка добавления метаданных выполнения: {}", e.getMessage());
        }
    }

    /**
     * Маппинг приоритета на уровень серьезности Allure
     */
    private SeverityLevel mapPriorityToSeverity(TestPriority.Priority priority) {
        switch (priority) {
            case CRITICAL:
                return SeverityLevel.BLOCKER;
            case HIGH:
                return SeverityLevel.CRITICAL;
            case MEDIUM:
                return SeverityLevel.NORMAL;
            case LOW:
                return SeverityLevel.MINOR;
            case TRIVIAL:
                return SeverityLevel.TRIVIAL;
            default:
                return SeverityLevel.NORMAL;
        }
    }

    /**
     * Построение описания окружения
     */
    private String buildEnvironmentDescription(TestEnvironment annotation) {
        StringBuilder env = new StringBuilder();

        if (annotation.browsers().length > 0) {
            env.append("Browsers: ").append(Arrays.toString(annotation.browsers())).append("; ");
        }

        if (annotation.operatingSystems().length > 0) {
            env.append("OS: ").append(Arrays.toString(annotation.operatingSystems())).append("; ");
        }

        if (!annotation.minJavaVersion().isEmpty()) {
            env.append("Java: ").append(annotation.minJavaVersion()).append("+");
        }

        return env.toString();
    }

    private String getTestId(ExtensionContext context) {
        return context.getRequiredTestClass().getName() + "#" + context.getRequiredTestMethod().getName();
    }
}
