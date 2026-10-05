package com.bft.jupiter.extension;

import com.bft.jupiter.annotation.EnabledOnEnvironment;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;

/**
 * JUnit 5 extension that enables tests only on specified environments.
 */
public class EnabledOnEnvironmentExtension implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        String currentEnv = DisabledOnEnvironmentExtension.resolveEnvironment();

        EnabledOnEnvironment annotation = context.getRequiredTestMethod()
                .getAnnotation(EnabledOnEnvironment.class);
        if (annotation == null) {
            annotation = context.getRequiredTestClass()
                    .getAnnotation(EnabledOnEnvironment.class);
        }

        if (annotation == null) {
            return ConditionEvaluationResult.enabled("No @EnabledOnEnvironment annotation found");
        }

        boolean enabled = Arrays.stream(annotation.value())
                .map(String::trim)
                .anyMatch(env -> env.equalsIgnoreCase(currentEnv));

        if (enabled) {
            return ConditionEvaluationResult.enabled(
                    String.format("Enabled on environment '%s'", currentEnv));
        } else {
            return ConditionEvaluationResult.disabled(
                    String.format("Disabled: only enabled on %s, current is '%s'",
                            Arrays.toString(annotation.value()), currentEnv));
        }
    }
}
