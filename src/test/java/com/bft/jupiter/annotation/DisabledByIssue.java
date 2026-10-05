package com.bft.jupiter.annotation;

import com.bft.jupiter.extension.DisabledByIssueExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method-level annotation that disables a test when a linked Jira issue is still open.
 *
 * <p>Automatically re-enables the test once the bug is closed/resolved.
 * Useful for temporarily disabling known broken tests without removing them.
 *
 * <p>Example usage:
 * <pre>{@code
 * @DisabledByIssue(
 *     value = "Button click does not work on Firefox",
 *     issueKey = "EVS-1234"
 * )
 * @Test
 * void testFirefoxButtonClick() { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DisabledByIssueExtension.class)
public @interface DisabledByIssue {
    /**
     * Human-readable description of the issue.
     */
    String value();

    /**
     * The Jira issue key (e.g., "EVS-1234").
     */
    String issueKey();
}
