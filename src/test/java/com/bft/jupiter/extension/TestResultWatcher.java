package com.bft.jupiter.extension;

import com.bft.helpers.TelegramNotifier;
import com.bft.integration.mapper.TestStatus;
import com.bft.integration.tms.TmsClient;
import com.bft.integration.tms.TmsConfig;
import io.qameta.allure.TmsLink;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Global test watcher that logs test execution results, sends Telegram notifications
 * and reports statuses to Jira TMS.
 *
 * <p>TMS reporting is performed only when:
 * <ul>
 *   <li>the test method is annotated with {@code @TmsLink("EVS-T-NNN")}</li>
 *   <li>TMS is configured (tms.url, tms.token, tms.test-run-id)</li>
 * </ul>
 *
 * <p>Registers as a global extension via junit-platform.properties.
 */
public class TestResultWatcher implements TestWatcher {

    private static final Logger log = LoggerFactory.getLogger(TestResultWatcher.class);

    private static final AtomicInteger totalTests = new AtomicInteger(0);
    private static final AtomicInteger passedTests = new AtomicInteger(0);
    private static final AtomicInteger failedTests = new AtomicInteger(0);
    private static final AtomicInteger skippedTests = new AtomicInteger(0);

    private static volatile TelegramNotifier telegramNotifier;

    /** гарантирует однократную отправку итоговой сводки при завершении JVM */
    private static final AtomicBoolean finalSummarySent = new AtomicBoolean(false);

    static {
        // Итоговая сводка обязана уйти ВСЕГДА, даже когда тестов было меньше 10
        // (порог прогресс-сводки) или JVM завершается аварийно.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (finalSummarySent.compareAndSet(false, true)) {
                int total = totalTests.get();
                if (total > 0) {
                    getTelegramNotifier().sendSummary(
                            total, passedTests.get(), failedTests.get(), skippedTests.get());
                    log.info("=== FINAL SUMMARY: total={}, passed={}, failed={}, skipped={} ===",
                            total, passedTests.get(), failedTests.get(), skippedTests.get());
                }
            }
        }, "telegram-final-summary"));
    }

    private static TelegramNotifier getTelegramNotifier() {
        if (telegramNotifier == null) {
            synchronized (TestResultWatcher.class) {
                if (telegramNotifier == null) {
                    telegramNotifier = new TelegramNotifier();
                }
            }
        }
        return telegramNotifier;
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        String testName = getTestName(context);
        log.info("TEST PASSED: {}", testName);
        totalTests.incrementAndGet();
        passedTests.incrementAndGet();
        getTelegramNotifier().sendTestResult(testName, true, null);
        reportToTms(context, TestStatus.PASS, null);
        sendSummaryIfNeeded();
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        String testName = getTestName(context);
        log.error("TEST FAILED: {} - {}", testName, cause.getMessage());
        totalTests.incrementAndGet();
        failedTests.incrementAndGet();
        String comment = cause.getMessage() == null ? "Failed" : cause.getMessage();
        getTelegramNotifier().sendTestResult(testName, false, comment);
        reportToTms(context, TestStatus.FAIL, comment);
        sendSummaryIfNeeded();
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        String testName = getTestName(context);
        String reasonStr = reason.orElse("no reason specified");
        log.info("TEST SKIPPED: {} - {}", testName, reasonStr);
        totalTests.incrementAndGet();
        skippedTests.incrementAndGet();
        reportToTms(context, TestStatus.SKIP, reasonStr);
    }

    /**
     * Reports the result to Jira TMS when the test has a @TmsLink annotation
     * and TMS integration is configured. Never throws.
     */
    private void reportToTms(ExtensionContext context, TestStatus status, String comment) {
        try {
            String tmsLink = resolveTmsLink(context);
            if (tmsLink == null) {
                return;
            }
            TmsConfig config = TmsConfig.getInstance();
            if (!config.isEnabled()) {
                log.debug("TMS: not configured, skipping report for {}", tmsLink);
                return;
            }
            new TmsClient(config).reportResult(tmsLink, status, comment);
        } catch (Exception e) {
            log.warn("TMS: unexpected error while reporting {}: {}", status, e.getMessage());
        }
    }

    private String resolveTmsLink(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        TmsLink annotation = method.getAnnotation(TmsLink.class);
        if (annotation != null && !annotation.value().isEmpty()) {
            return annotation.value();
        }
        Class<?> clazz = context.getRequiredTestClass();
        TmsLink classAnnotation = clazz.getAnnotation(TmsLink.class);
        if (classAnnotation != null && !classAnnotation.value().isEmpty()) {
            return classAnnotation.value();
        }
        return null;
    }

    /**
     * Sends progress summary every 10 completed tests.
     */
    private void sendSummaryIfNeeded() {
        int total = totalTests.get();
        if (total > 0 && total % 10 == 0) {
            int passed = passedTests.get();
            int failed = failedTests.get();
            int skipped = skippedTests.get();
            log.info("=== PROGRESS SUMMARY: total={}, passed={}, failed={}, skipped={} ===",
                    total, passed, failed, skipped);
            getTelegramNotifier().sendSummary(total, passed, failed, skipped);
        }
    }

    /**
     * Resets counters (useful for parallel test execution).
     */
    public static void resetCounters() {
        totalTests.set(0);
        passedTests.set(0);
        failedTests.set(0);
        skippedTests.set(0);
    }

    private String getTestName(ExtensionContext context) {
        return context.getRequiredTestClass().getSimpleName()
                + "." + context.getRequiredTestMethod().getName();
    }
}
