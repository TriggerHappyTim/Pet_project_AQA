package com.bft.pw.logging;

/**
 * Типы логов браузера (совместимость с {@code org.openqa.selenium.logging.LogType}).
 */
public final class LogType {

    public static final String BROWSER = "browser";
    public static final String CLIENT = "client";
    public static final String DRIVER = "driver";
    public static final String PERFORMANCE = "performance";
    public static final String PROFILER = "profiler";
    public static final String SERVER = "server";

    private LogType() {
    }
}