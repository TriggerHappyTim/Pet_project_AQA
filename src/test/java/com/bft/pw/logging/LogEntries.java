package com.bft.pw.logging;

import java.util.List;

/**
 * Коллекция записей логов браузера.
 */
public class LogEntries {

    private final List<LogEntry> entries;

    public LogEntries(List<LogEntry> entries) {
        this.entries = entries;
    }

    public List<LogEntry> getAll() {
        return entries;
    }
}