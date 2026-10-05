package com.bft.service.kafka;

/**
 * Configuration for Kafka event verification.
 *
 * <p>Resolution order: System Property > Environment Variable > Default.
 *
 * <p>Properties:
 * <ul>
 *   <li>{@code kafka.servers} / {@code KAFKA_SERVERS} — bootstrap servers,
 *       e.g. {@code 172.18.38.9:9092} (int) or {@code localhost:29093} (local)</li>
 *   <li>{@code kafka.poll-interval-ms} / {@code KAFKA_POLL_INTERVAL_MS} —
 *       poll interval, default 500 ms</li>
 * </ul>
 *
 * <p>Per-environment defaults (from application-*.yml):
 * <pre>
 *   dev:   172.18.32.223:9092
 *   int:   172.18.38.9:9092
 *   local: localhost:29093
 * </pre>
 */
public final class KafkaConfig {

    private static volatile KafkaConfig instance;

    private final String bootstrapServers;
    private final long pollIntervalMs;

    private KafkaConfig() {
        this.bootstrapServers = resolve("kafka.servers", "KAFKA_SERVERS", "localhost:9092");
        this.pollIntervalMs = resolveLong("kafka.poll-interval-ms", "KAFKA_POLL_INTERVAL_MS", 500);
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public long getPollIntervalMs() {
        return pollIntervalMs;
    }

    public static KafkaConfig getInstance() {
        if (instance == null) {
            synchronized (KafkaConfig.class) {
                if (instance == null) {
                    instance = new KafkaConfig();
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

    private long resolveLong(String sysProp, String envVar, long defaultValue) {
        String value = resolve(sysProp, envVar, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
