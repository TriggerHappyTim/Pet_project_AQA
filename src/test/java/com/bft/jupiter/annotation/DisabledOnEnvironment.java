package com.bft.jupiter.annotation;

import com.bft.jupiter.extension.DisabledOnEnvironmentExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Disables test when running on specified environments.
 *
 * <p>Example usage:
 * <pre>{@code
 * @DisabledOnEnvironment({"prod", "int"})
 * @Test
 * void testOnlyOnDevAndTest() { ... }
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DisabledOnEnvironmentExtension.class)
public @interface DisabledOnEnvironment {
    /**
     * Environments on which the test should be disabled.
     * Matches against the "environment" system property or ENVIRONMENT env var.
     */
    String[] value();
}
