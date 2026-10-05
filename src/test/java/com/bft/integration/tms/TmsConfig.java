package com.bft.integration.tms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for Jira TMS (Test Management System) integration.
 *
 * <p>Resolution order: System Property > Environment Variable > Default.
 *
 * <p>Required properties for enabling integration:
 * <ul>
 *   <li>{@code tms.url} / {@code TMS_URL} — base URL (e.g., https://jira.example.local)</li>
 *   <li>{@code tms.token} / {@code TMS_TOKEN} — Bearer token</li>
 *   <li>{@code tms.test-run-id} / {@code TMS_TEST_RUN_ID} — test run to update</li>
 * </ul>
 *
 * <p>Optional:
 * <ul>
 *   <li>{@code tms.project-id} / {@code TMS_PROJECT_ID} — Jira project ID</li>
 *   <li>{@code tms.user-key} / {@code TMS_USER_KEY} — executor user key</li>
 *   <li>{@code tms.enabled} / {@code TMS_ENABLED} — master switch (default: auto)</li>
 * </ul>
 */
public final class TmsConfig {

    private static final Logger log = LoggerFactory.getLogger(TmsConfig.class);

    private static volatile TmsConfig instance;

    private final String baseUrl;
    private final String token;
    private final String testRunId;
    private final String projectId;
    private final String userKey;

    private TmsConfig() {
        this.baseUrl = trimTrailingSlash(resolve("tms.url", "TMS_URL", null));
        this.token = resolve("tms.token", "TMS_TOKEN", null);
        this.testRunId = resolve("tms.test-run-id", "TMS_TEST_RUN_ID", null);
        this.projectId = resolve("tms.project-id", "TMS_PROJECT_ID", "");
        this.userKey = resolve("tms.user-key", "TMS_USER_KEY", "");
    }

    /**
     * Returns true if TMS integration is fully configured and should be used.
     */
    public boolean isEnabled() {
        String masterSwitch = resolve("tms.enabled", "TMS_ENABLED", null);
        if ("false".equalsIgnoreCase(masterSwitch)) {
            return false;
        }
        return baseUrl != null && token != null && testRunId != null;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getToken() {
        return token;
    }

    public String getTestRunId() {
        return testRunId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getUserKey() {
        return userKey;
    }

    public static TmsConfig getInstance() {
        if (instance == null) {
            synchronized (TmsConfig.class) {
                if (instance == null) {
                    instance = new TmsConfig();
                    if (instance.isEnabled()) {
                        log.info("TMS integration enabled: url={}, testRun={}",
                                instance.baseUrl, instance.testRunId);
                    } else {
                        log.debug("TMS integration disabled (tms.url/tms.token/tms.test-run-id not fully configured)");
                    }
                }
            }
        }
        return instance;
    }

    /** Resets cached config (for tests). */
    static void reset() {
        instance = null;
    }

    private String resolve(String sysProp, String envVar, String defaultValue) {
        String value = System.getProperty(sysProp);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        value = System.getenv(envVar);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return defaultValue;
    }

    private String trimTrailingSlash(String url) {
        if (url == null) return null;
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
