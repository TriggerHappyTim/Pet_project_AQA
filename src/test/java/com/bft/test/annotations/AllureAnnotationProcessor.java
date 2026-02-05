package com.bft.test.annotations;

import io.qameta.allure.Allure;
import io.qameta.allure.SeverityLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/**
 * Обработчик кастомных аннотаций для интеграции с Allure
 * Автоматически добавляет информацию из аннотаций в Allure отчеты
 */
public class AllureAnnotationProcessor implements IInvokedMethodListener {

    private static final Logger logger = LoggerFactory.getLogger(AllureAnnotationProcessor.class);

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (!method.isTestMethod()) {
            return;
        }

        try {
            processTestAnnotations(method.getTestMethod().getConstructorOrMethod().getMethod(), testResult.getInstance());
        } catch (Exception e) {
            logger.warn("Ошибка обработки аннотаций для теста: {}", e.getMessage());
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        // Дополнительная обработка после выполнения теста
        if (method.isTestMethod()) {
            attachExecutionMetadata(testResult);
        }
    }

    /**
     * Обработка всех кастомных аннотаций теста
     */
    private void processTestAnnotations(Method method, Object testInstance) {
        Class<?> testClass = testInstance.getClass();

        // Обработка аннотаций класса
        processClassAnnotations(testClass);

        // Обработка аннотаций метода
        processMethodAnnotations(method);

        // Создание комплексного описания
        createComprehensiveDescription(method, testClass);
    }

    /**
     * Обработка аннотаций класса
     */
    private void processClassAnnotations(Class<?> testClass) {
        // TestType аннотация
        Optional.ofNullable(testClass.getAnnotation(TestType.class))
            .ifPresent(annotation -> {
                Allure.epic(annotation.value().getDisplayName());
                if (!annotation.description().isEmpty()) {
                    Allure.description("Тип теста: " + annotation.description());
                }
            });

        // TestPriority аннотация
        Optional.ofNullable(testClass.getAnnotation(TestPriority.class))
            .ifPresent(annotation -> {
                SeverityLevel severity = mapPriorityToSeverity(annotation.value());
                Allure.label("severity", severity.name().toLowerCase());
            });

        // Requirement аннотация
        Optional.ofNullable(testClass.getAnnotation(Requirement.class))
            .ifPresent(annotation -> {
                Allure.label("requirement", annotation.id());
                Allure.label("requirement.name", annotation.name());
                Allure.label("requirement.source", annotation.source());
                Allure.label("requirement.version", annotation.version());
                Allure.label("requirement.status", annotation.status().name());
            });

        // TestEnvironment аннотация
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
        // TestPriority аннотация метода (переопределяет класс)
        Optional.ofNullable(method.getAnnotation(TestPriority.class))
            .ifPresent(annotation -> {
                SeverityLevel severity = mapPriorityToSeverity(annotation.value());
                Allure.label("severity", severity.name().toLowerCase());

                if (!annotation.reason().isEmpty()) {
                    Allure.description("Причина приоритета: " + annotation.reason());
                }
            });

        // Requirement аннотация метода (переопределяет класс)
        Optional.ofNullable(method.getAnnotation(Requirement.class))
            .ifPresent(annotation -> {
                Allure.label("requirement", annotation.id());
                Allure.label("requirement.name", annotation.name());
                Allure.label("requirement.version", annotation.version());
                Allure.label("requirement.status", annotation.status().name());
            });

        // AutomationAction аннотации
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

        // Добавление информации о типе теста
        Optional.ofNullable(testClass.getAnnotation(TestType.class))
            .ifPresent(annotation -> {
                description.append("**Тип теста:** ").append(annotation.value().getDisplayName());
                if (!annotation.description().isEmpty()) {
                    description.append(" - ").append(annotation.description());
                }
                description.append("\n\n");
            });

        // Добавление информации о приоритете
        Optional.ofNullable(method.getAnnotation(TestPriority.class))
            .ifPresent(annotation -> {
                description.append("**Приоритет:** ").append(annotation.value().getDescription());
                if (!annotation.reason().isEmpty()) {
                    description.append(" (").append(annotation.reason()).append(")");
                }
                description.append("\n\n");
            });

        // Добавление информации о требованиях
        Optional.ofNullable(method.getAnnotation(Requirement.class))
            .ifPresent(annotation -> {
                description.append("**Требование:** ").append(annotation.name())
                          .append(" (ID: ").append(annotation.id()).append(")\n");
                description.append("**Источник:** ").append(annotation.source()).append("\n");
                description.append("**Версия:** ").append(annotation.version()).append("\n");
                description.append("**Статус:** ").append(annotation.status().name()).append("\n\n");
            });

        // Добавление информации об окружении
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

        // Установка описания в Allure
        if (description.length() > 0) {
            Allure.description(description.toString());
        }
    }

    /**
     * Добавление метаданных выполнения
     */
    private void attachExecutionMetadata(ITestResult testResult) {
        try {
            // Добавление времени выполнения
            long duration = testResult.getEndMillis() - testResult.getStartMillis();
            Allure.label("execution.duration", String.valueOf(duration));
            
            // Правильная обработка статусов тестов, включая пропущенные (skipped)
            String status;
            switch (testResult.getStatus()) {
                case ITestResult.SUCCESS:
                    status = "passed";
                    break;
                case ITestResult.FAILURE:
                    status = "failed";
                    break;
                case ITestResult.SKIP:
                    status = "skipped";
                    // Убеждаемся, что пропущенные тесты также попадают в отчет
                    Allure.label("skip.reason", testResult.getThrowable() != null ? 
                        testResult.getThrowable().getMessage() : "Test was skipped");
                    break;
                default:
                    status = "unknown";
            }
            Allure.label("execution.status", status);

            // Добавление информации о методе
            Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
            Allure.label("method.signature", method.toString());

            // Добавление информации о классе
            Allure.label("class.name", testResult.getTestClass().getName());

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
}