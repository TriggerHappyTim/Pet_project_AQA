package com.bft.jupiter.extension;

import com.bft.jupiter.annotation.DisabledOnEnvironment;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;

/**
 * JUnit 5 extension that disables tests based on the current environment.
 *
 * <p>Reads the environment from the "environment" system property (set via -Denvironment=...)
 * or the "ENVIRONMENT" environment variable (set in CI). Falls back to "dev" if neither is set.
 */
public class DisabledOnEnvironmentExtension implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        String currentEnv = resolveEnvironment();

        // Check class-level annotation
        DisabledOnEnvironment classAnnotation = context.getRequiredTestClass()
                .getAnnotation(DisabledOnEnvironment.class);
        if (classAnnotation != null && isDisabled(currentEnv, classAnnotation.value())) {
            return ConditionEvaluationResult.disabled(
                    String.format("Disabled on environment '%s' (class-level)", currentEnv));
        }

        // Check method-level annotation
        DisabledOnEnvironment methodAnnotation = context.getRequiredTestMethod()
                .getAnnotation(DisabledOnEnvironment.class);
        if (methodAnnotation != null && isDisabled(currentEnv, methodAnnotation.value())) {
            return ConditionEvaluationResult.disabled(
                    String.format("Disabled on environment '%s' (method-level)", currentEnv));
        }

        return ConditionEvaluationResult.enabled(
                String.format("Enabled on environment '%s'", currentEnv));
    }

    private boolean isDisabled(String currentEnv, String[] disabledEnvs) {
        return Arrays.stream(disabledEnvs)
                .map(String::trim)
                .anyMatch(env -> env.equalsIgnoreCase(currentEnv));
    }

    static String resolveEnvironment() {
        String env = System.getProperty("environment");
        if (env == null || env.isEmpty()) {
            env = System.getenv("ENVIRONMENT");
        }
        if (env == null || env.isEmpty()) {
            env = "dev";
        }
        return env.trim().toLowerCase();
    }
}
