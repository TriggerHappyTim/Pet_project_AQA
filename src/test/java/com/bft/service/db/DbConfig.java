package com.bft.service.db;

/**
 * Configuration for direct database access (Apache Phoenix Query Server).
 *
 * <p>Resolution order: System Property > Environment Variable.
 *
 * <p>Properties:
 * <ul>
 *   <li>{@code db.url} / {@code DB_URL} — JDBC URL, e.g.
 *       {@code jdbc:phoenix:thin:url=http://172.18.36.33:8765;serialization=PROTOBUF}</li>
 *   <li>{@code db.user} / {@code DB_USER} — optional user name</li>
 *   <li>{@code db.password} / {@code DB_PASSWORD} — optional password</li>
 *   <li>{@code db.enabled} / {@code DB_ENABLED} — master switch (default: auto by URL presence)</li>
 * </ul>
 *
 * <p>Per-environment defaults (from application-*.yml):
 * <pre>
 *   dev:   jdbc:phoenix:thin:url=http://172.18.34.22:8765;serialization=PROTOBUF
 *   int:   jdbc:phoenix:thin:url=http://172.18.36.33:8765;serialization=PROTOBUF
 * </pre>
 */
public final class DbConfig {

    private static volatile DbConfig instance;

    private final String url;
    private final String user;
    private final String password;

    private DbConfig() {
        this.url = resolve("db.url", "DB_URL", null);
        this.user = resolve("db.user", "DB_USER", null);
        this.password = resolve("db.password", "DB_PASSWORD", null);
    }

    /**
     * Returns true if database access is configured and enabled.
     */
    public boolean isEnabled() {
        String masterSwitch = resolve("db.enabled", "DB_ENABLED", null);
        if ("false".equalsIgnoreCase(masterSwitch)) {
            return false;
        }
        return url != null && !url.isEmpty();
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public static DbConfig getInstance() {
        if (instance == null) {
            synchronized (DbConfig.class) {
                if (instance == null) {
                    instance = new DbConfig();
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
}
