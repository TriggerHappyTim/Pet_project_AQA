package com.bft.jupiter.annotation;

import com.bft.jupiter.extension.DisabledByInfrastructureImpl;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level annotation that disables ALL tests in a class if infrastructure health checks fail.
 *
 * <p>Performs pre-flight checks before any test runs. If a required service is unreachable,
 * all tests in the class are skipped rather than failing with connection errors.
 *
 * <p>Example usage:
 * <pre>{@code
 * @DisabledByInfrastructure(services = {"graphql-mesh", "grpc-metadata"})
 * public class MyIntegrationTest extends UITestBase {
 *     @Test
 *     void testSomething() { ... }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DisabledByInfrastructureImpl.class)
public @interface DisabledByInfrastructure {
    /**
     * List of service names to check before running tests.
     * These should match keys in your service endpoint configuration.
     */
    String[] services() default {};
}
