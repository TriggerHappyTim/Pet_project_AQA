package com.bft.jupiter.extension;

import com.bft.jupiter.annotation.DisabledByInfrastructure;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * JUnit 5 extension that checks infrastructure health before running tests.
 *
 * <p>If any required service is unreachable, all tests in the annotated class are disabled.
 */
public class DisabledByInfrastructureImpl implements ExecutionCondition {

    private static final Logger log = LoggerFactory.getLogger(DisabledByInfrastructureImpl.class);
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        DisabledByInfrastructure annotation = context.getRequiredTestClass()
                .getAnnotation(DisabledByInfrastructure.class);

        if (annotation == null) {
            return ConditionEvaluationResult.enabled("No @DisabledByInfrastructure annotation");
        }

        String[] services = annotation.services();
        if (services.length == 0) {
            return ConditionEvaluationResult.enabled("No services to check");
        }

        String environment = DisabledOnEnvironmentExtension.resolveEnvironment();

        // Skip infrastructure checks in local/dev environments (services may not be available)
        if ("dev".equals(environment) || "local".equals(environment)) {
            log.info("Skipping infrastructure check for environment '{}'", environment);
            return ConditionEvaluationResult.enabled(
                    "Infrastructure check skipped for environment: " + environment);
        }

        log.info("Checking infrastructure for environment '{}' - services: {}",
                environment, Arrays.toString(services));

        for (String service : services) {
            try {
                checkServiceHealth(service, environment);
            } catch (Exception e) {
                String message = String.format("Infrastructure check FAILED for service '%s': %s",
                        service, e.getMessage());
                log.error(message);
                return ConditionEvaluationResult.disabled(message);
            }
        }

        return ConditionEvaluationResult.enabled("All infrastructure checks passed");
    }

    private void checkServiceHealth(String serviceName, String environment) throws Exception {
        String healthUrl = buildHealthUrl(serviceName, environment);
        log.debug("Checking health of {} at {}", serviceName, healthUrl);

        HttpURLConnection connection = (HttpURLConnection) new URL(healthUrl).openConnection();
        try {
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new Exception(String.format(
                        "Health check returned HTTP %d for %s", responseCode, serviceName));
            }
            log.debug("Service {} is healthy (HTTP 200)", serviceName);
        } finally {
            connection.disconnect();
        }
    }

    private String buildHealthUrl(String serviceName, String environment) {
        // Use environment properties to build health check URL
        String baseUrl = System.getProperty("service." + serviceName + ".healthUrl");
        if (baseUrl == null) {
            baseUrl = System.getenv("SERVICE_" + serviceName.toUpperCase().replace("-", "_") + "_HEALTH_URL");
        }
        if (baseUrl != null) {
            return baseUrl;
        }
        // Fallback: construct URL from convention
        return String.format("https://%s.%s.ecp/actuator/health", serviceName, environment);
    }
}
