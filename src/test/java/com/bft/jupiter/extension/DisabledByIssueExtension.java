package com.bft.jupiter.extension;

import com.bft.jupiter.annotation.DisabledByIssue;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit 5 extension that disables tests based on linked Jira issue status.
 *
 * <p>Checks if the linked issue is still open. If so, the test is disabled.
 * If the issue is closed/resolved, the test is enabled.
 *
 * <p>Currently uses a simple check: if DISABLE_JIRA_CHECK=true (system property or env var),
 * all tests with @DisabledByIssue are enabled (for local development).
 * TODO: Implement actual Jira REST API integration.
 */
public class DisabledByIssueExtension implements ExecutionCondition {

    private static final Logger log = LoggerFactory.getLogger(DisabledByIssueExtension.class);

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        DisabledByIssue annotation = context.getRequiredTestMethod()
                .getAnnotation(DisabledByIssue.class);

        if (annotation == null) {
            return ConditionEvaluationResult.enabled("No @DisabledByIssue annotation");
        }

        String issueKey = annotation.issueKey();
        String description = annotation.value();

        // Allow bypassing Jira check in local development
        boolean skipCheck = Boolean.parseBoolean(
                getPropertyOrEnv("DISABLE_JIRA_CHECK", "false"));

        if (skipCheck) {
            log.info("Jira check disabled via DISABLE_JIRA_CHECK. Test enabled: {} [{}]",
                    context.getRequiredTestMethod().getName(), issueKey);
            return ConditionEvaluationResult.enabled(
                    "Jira check disabled (DISABLE_JIRA_CHECK=true). Issue: " + issueKey);
        }

        // TODO: Implement actual Jira REST API call to check issue status
        // For now, always disable when annotation is present (conservative approach)
        log.warn("Test disabled by issue: {} - {} [{}]",
                issueKey, description, context.getRequiredTestMethod().getName());
        return ConditionEvaluationResult.disabled(
                String.format("Disabled by issue %s: %s", issueKey, description));
    }

    private String getPropertyOrEnv(String name, String defaultValue) {
        String value = System.getProperty(name);
        if (value == null || value.isEmpty()) {
            value = System.getenv(name);
        }
        return value != null ? value : defaultValue;
    }
}
