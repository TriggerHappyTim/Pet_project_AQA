package com.bft.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for resolving configuration from system properties and environment variables.
 *
 * <p>Resolution order: System Property > Environment Variable > Default Value.
 * This allows clean configuration in both local (via -D flags) and CI (via env vars) runs.
 *
 * <p>Example:
 * <pre>{@code
 * String env = EnvironmentUtils.getPropertyOrEnv("environment", "dev");
 * boolean headless = EnvironmentUtils.getBooleanPropertyOrEnv("headless", false);
 * }</pre>
 */
public final class EnvironmentUtils {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentUtils.class);

    private EnvironmentUtils() {
        // Utility class
    }

    /**
     * Resolves a property value from system property, then environment variable, then default.
     *
     * @param name         the property/variable name
     * @param defaultValue fallback value
     * @return resolved value
     */
    public static String getPropertyOrEnv(String name, String defaultValue) {
        // 1. System property (-Dname=value)
        String value = System.getProperty(name);
        if (value != null && !value.isEmpty()) {
            log.trace("Resolved '{}' from system property: {}", name, value);
            return value;
        }

        // 2. Environment variable
        value = System.getenv(name);
        if (value != null && !value.isEmpty()) {
            log.trace("Resolved '{}' from environment variable: {}", name, value);
            return value;
        }

        // 3. Default
        log.trace("Using default for '{}': {}", name, defaultValue);
        return defaultValue;
    }

    /**
     * Convenience overload without default (returns null if not found).
     */
    public static String getPropertyOrEnv(String name) {
        return getPropertyOrEnv(name, null);
    }

    /**
     * Resolves a boolean property value.
     *
     * @param name         the property/variable name
     * @param defaultValue fallback value
     * @return resolved boolean value
     */
    public static boolean getBooleanPropertyOrEnv(String name, boolean defaultValue) {
        String value = getPropertyOrEnv(name);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Resolves an integer property value.
     *
     * @param name         the property/variable name
     * @param defaultValue fallback value
     * @return resolved integer value
     */
    public static int getIntPropertyOrEnv(String name, int defaultValue) {
        String value = getPropertyOrEnv(name);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid integer for property '{}': {}, using default: {}", name, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Returns the current environment name (dev/test/uat/int/prod).
     */
    public static String getCurrentEnvironment() {
        return getPropertyOrEnv("environment", "dev").toLowerCase().trim();
    }

    /**
     * Returns true if running in local or dev environment.
     */
    public static boolean isLocalOrDev() {
        String env = getCurrentEnvironment();
        return "dev".equals(env) || "local".equals(env);
    }

    /**
     * Returns true if running in a CI environment (HELM_NAMESPACE is set).
     */
    public static boolean isCiEnvironment() {
        return getPropertyOrEnv("HELM_NAMESPACE") != null;
    }
}
