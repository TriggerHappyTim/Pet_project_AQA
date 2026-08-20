package com.bft.pw.logging;

/**
 * Запись лога браузера (совместимость с {@code org.openqa.selenium.logging.LogEntry}).
 */
public class LogEntry {

    private final long timestamp;
    private final String level;
    private final String message;

    public LogEntry(String message) {
        this(System.currentTimeMillis(), "INFO", message);
    }

    public LogEntry(long timestamp, String level, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}