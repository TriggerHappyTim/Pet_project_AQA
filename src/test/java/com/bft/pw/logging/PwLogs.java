package com.bft.pw.logging;

import com.bft.pw.PwSession;

/**
 * Логи браузера, собранные через Playwright console events.
 */
public class PwLogs {

    public LogEntries get(String logType) {
        return new LogEntries(PwSession.consoleLogs());
    }

    public LogEntries getAvailableLogTypes() {
        return new LogEntries(PwSession.consoleLogs());
    }
}