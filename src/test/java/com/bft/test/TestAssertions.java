package com.bft.test;

import com.bft.utils.DebugUtils;
import com.bft.pw.Selenide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static com.bft.pw.Condition.exist;

/**
 * Custom soft assertions for JUnit 5 (replaces TestNG SoftAssert).
 *
 * Collects all assertion failures and reports them at once via {@link #assertAll()}.
 * Automatically takes a screenshot on the first failure.
 */
public class TestAssertions {

    private static final Logger log = LoggerFactory.getLogger(TestAssertions.class);

    private static final ThreadLocal<List<AssertionError>> COLLECTED =
            ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<Boolean> SCREENSHOT_TAKEN =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    /**
     * Clears collected failures and the screenshot flag for the current thread.
     * Called before each test so failures never leak between tests.
     */
    public static void reset() {
        COLLECTED.get().clear();
        SCREENSHOT_TAKEN.set(Boolean.FALSE);
    }

    private static void record(AssertionError error) {
        COLLECTED.get().add(error);
    }

    /**
     * Verifies a condition is true, recording a failure if not.
     */
    public void assertTrue(boolean condition, String message) {
        if (!condition) {
            handleFailure(message);
            record(new AssertionError(message));
        }
    }

    /**
     * Verifies a condition is false, recording a failure if not.
     */
    public void assertFalse(boolean condition, String message) {
        if (condition) {
            handleFailure(message);
            record(new AssertionError(message));
        }
    }

    /**
     * Verifies two objects are equal, recording a failure if not.
     */
    public void assertEquals(Object actual, Object expected, String message) {
        if (!Objects.equals(actual, expected)) {
            String fullMessage = message + String.format(" (Expected: '%s', Actual: '%s')", expected, actual);
            handleFailure(fullMessage);
            record(new AssertionError(fullMessage));
        }
    }

    /**
     * Verifies two objects are not equal, recording a failure if they are.
     */
    public void assertNotEquals(Object actual, Object expected, String message) {
        if (Objects.equals(actual, expected)) {
            String fullMessage = message + String.format(" (Both values: '%s')", actual);
            handleFailure(fullMessage);
            record(new AssertionError(fullMessage));
        }
    }

    /**
     * Verifies an object is not null, recording a failure if it is.
     */
    public void assertNotNull(Object actual, String message) {
        if (actual == null) {
            handleFailure(message);
            record(new AssertionError(message));
        }
    }

    /**
     * Records a failure with the given message immediately (like JUnit Assertions.fail).
     */
    public void fail(String message) {
        handleFailure(message);
        record(new AssertionError(message));
    }

    /**
     * Verifies element is visible on the page.
     */
    public void assertVisible(com.bft.pw.SelenideElement element, String elementName) {
        String msg = "Element '" + elementName + "' should be visible";
        try {
            element.shouldBe(com.bft.pw.Condition.visible);
        } catch (AssertionError e) {
            handleFailure(msg);
            record(new AssertionError(msg, e));
        }
    }

    /**
     * Verifies element does not exist on the page.
     */
    public void assertNotExists(com.bft.pw.SelenideElement element, String elementName) {
        String msg = "Element '" + elementName + "' should not exist on the page";
        try {
            element.shouldNot(exist);
        } catch (AssertionError e) {
            handleFailure(msg);
            record(new AssertionError(msg, e));
        }
    }

    /**
     * Reports all collected assertion failures. Call this at the end of each test.
     *
     * @throws AssertionError if any assertions failed
     */
    public void assertAll() {
        List<AssertionError> errors = COLLECTED.get();
        try {
            if (!errors.isEmpty()) {
                AssertionError combined = new AssertionError(
                        errors.size() + " assertion(s) failed. First: " + errors.get(0).getMessage());
                errors.forEach(combined::addSuppressed);
                throw combined;
            }
        } finally {
            errors.clear();
            SCREENSHOT_TAKEN.set(Boolean.FALSE);
        }
    }

    private void handleFailure(String message) {
        log.error("ASSERTION FAILED: {}", message);
        if (!SCREENSHOT_TAKEN.get()) {
            try {
                String screenshotName = "failure_assert_" + System.currentTimeMillis();
                Selenide.screenshot(screenshotName);
                DebugUtils.savePageStateOnError("assert-failure-" + screenshotName, null);
                SCREENSHOT_TAKEN.set(Boolean.TRUE);
                log.info("Screenshot saved: {}", screenshotName);
            } catch (Exception e) {
                log.warn("Failed to take screenshot on assertion failure: {}", e.getMessage());
            }
        }
    }
}