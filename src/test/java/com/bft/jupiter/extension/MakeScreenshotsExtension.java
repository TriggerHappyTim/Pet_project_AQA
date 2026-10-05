package com.bft.jupiter.extension;

import com.bft.pw.Selenide;
import com.bft.utils.DebugUtils;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

/**
 * JUnit 5 extension that automatically captures a screenshot and page source on test failure.
 *
 * <p>Attaches the screenshot to the Allure report for easy debugging.
 */
public class MakeScreenshotsExtension implements AfterTestExecutionCallback {

    private static final Logger log = LoggerFactory.getLogger(MakeScreenshotsExtension.class);

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        if (context.getExecutionException().isPresent()) {
            Throwable exception = context.getExecutionException().get();
            String testName = context.getRequiredTestMethod().getName();
            String testClass = context.getRequiredTestClass().getSimpleName();

            log.error("Test FAILED: {}.{} - {}", testClass, testName, exception.getMessage());

            try {
                // Take screenshot
                String screenshotName = String.format("failure_%s_%s_%d",
                        testClass, testName, System.currentTimeMillis());
                String screenshotPath = Selenide.screenshot(screenshotName);

                if (screenshotPath != null && !screenshotPath.isEmpty()) {
                    File screenshotFile = new File(screenshotPath);
                    if (screenshotFile.exists()) {
                        try (FileInputStream fis = new FileInputStream(screenshotFile)) {
                            Allure.addAttachment("Screenshot on failure",
                                    "image/png", fis, ".png");
                        }
                        log.info("Screenshot attached to Allure: {}", screenshotPath);
                    }
                }

                // Attach page source
                String pageSource = com.bft.pw.WebDriverRunner.source();
                if (pageSource != null && !pageSource.isEmpty()) {
                    Allure.addAttachment("Page source on failure",
                            "text/html", new ByteArrayInputStream(
                            pageSource.getBytes(StandardCharsets.UTF_8)), ".html");
                }

                // Save debug state
                DebugUtils.savePageStateOnError(
                        String.format("%s.%s", testClass, testName), exception);

            } catch (Exception e) {
                log.warn("Failed to capture screenshot on test failure: {}", e.getMessage());
            }
        }
    }
}
