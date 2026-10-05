package com.bft.helpers;

import com.bft.config.EnvironmentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Pre-flight check class that validates service dependencies before tests run.
 *
 * <p>Checks that required backend services are reachable and healthy.
 * Used by {@link com.bft.jupiter.annotation.DisabledByInfrastructure} to skip tests
 * when infrastructure is down.
 *
 * <p>Service URLs are resolved from system properties or environment variables:
 * <ul>
 *   <li>System property: {@code -Dservice.<name>.healthUrl=http://...}</li>
 *   <li>Environment variable: {@code SERVICE_<NAME>_HEALTH_URL=http://...}</li>
 * </ul>
 */
public class InfrastructureCheck {

    private static final Logger log = LoggerFactory.getLogger(InfrastructureCheck.class);
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;

    private final List<String> failures = new ArrayList<>();

    /**
     * Checks health of a named service.
     *
     * @param serviceName logical name of the service (e.g., "graphql-mesh")
     */
    public void checkService(String serviceName) {
        String healthUrl = resolveHealthUrl(serviceName);
        if (healthUrl == null) {
            log.warn("No health URL configured for service '{}', skipping check", serviceName);
            return;
        }

        try {
            checkHttpHealth(serviceName, healthUrl);
        } catch (Exception e) {
            String message = String.format("Service '%s' health check failed at %s: %s",
                    serviceName, healthUrl, e.getMessage());
            log.error(message);
            failures.add(message);
        }
    }

    /**
     * Checks multiple services and reports all failures.
     *
     * @param serviceNames list of service names to check
     * @throws AssertionError if any service health check fails
     */
    public void checkAll(String... serviceNames) {
        failures.clear();
        for (String service : serviceNames) {
            checkService(service);
        }

        if (!failures.isEmpty()) {
            String summary = String.format("%d service(s) failed health check:\n  - %s",
                    failures.size(), String.join("\n  - ", failures));
            throw new AssertionError(summary);
        }
        log.info("All {} service(s) passed health check", serviceNames.length);
    }

    /**
     * Returns true if we're in an environment where infrastructure checks should be skipped.
     */
    public boolean shouldSkipChecks() {
        String env = EnvironmentUtils.getCurrentEnvironment();
        return "dev".equals(env) || "local".equals(env);
    }

    /**
     * Returns true if the graphql-mesh service responds at all (any HTTP code counts:
     * even 404 on GET proves the service is up — mesh only accepts POST /graphql).
     */
    public boolean isGraphQLSigningAvailable() {
        String url = com.bft.service.graphql.GraphQLClient.resolveMeshUrl();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            connection.disconnect();
            log.debug("GraphQL mesh responded with HTTP {}", code);
            return true;
        } catch (Exception e) {
            log.debug("GraphQL mesh not available: {}", e.getMessage());
            return false;
        }
    }

    private void checkHttpHealth(String serviceName, String healthUrl) throws Exception {
        log.debug("Checking health of '{}' at {}", serviceName, healthUrl);

        HttpURLConnection connection = (HttpURLConnection) new URL(healthUrl).openConnection();
        try {
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                log.debug("Service '{}' is healthy (HTTP {})", serviceName, responseCode);
            } else {
                throw new Exception("HTTP " + responseCode);
            }
        } finally {
            connection.disconnect();
        }
    }

    private String resolveHealthUrl(String serviceName) {
        // 1. System property
        String key = "service." + serviceName + ".healthUrl";
        String url = System.getProperty(key);
        if (url != null && !url.isEmpty()) {
            return url;
        }

        // 2. Environment variable
        String envKey = "SERVICE_" + serviceName.toUpperCase().replace("-", "_") + "_HEALTH_URL";
        url = System.getenv(envKey);
        if (url != null && !url.isEmpty()) {
            return url;
        }

        return null;
    }
}
