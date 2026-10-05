package com.bft.jupiter.annotation;

import com.bft.jupiter.extension.EnabledOnEnvironmentExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables test ONLY on specified environments. All other environments will skip it.
 *
 * <p>Example usage:
 * <pre>{@code
 * @EnabledOnEnvironment({"test", "uat"})
 * @Test
 * void integrationTest() { ... }
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(EnabledOnEnvironmentExtension.class)
public @interface EnabledOnEnvironment {
    /**
     * Environments on which the test should run. All others will be skipped.
     */
    String[] value();
}
