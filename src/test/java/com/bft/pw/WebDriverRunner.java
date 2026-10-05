package com.bft.pw;

import com.bft.utils.FormStructureParser;

/**
 * Фасад для доступа к драйверу, совместимый с Selenide {@code WebDriverRunner}.
 */
public final class WebDriverRunner {

    private WebDriverRunner() {
    }

    public static PwDriver getWebDriver() {
        return PwSession.driver();
    }

    public static PwDriver getAndCheckWebDriver() {
        return PwSession.driver();
    }

    public static boolean hasWebDriverStarted() {
        return PwSession.isConfigured();
    }

    public static boolean isBrowserOpen() {
        return PwSession.isConfigured();
    }

    public static String url() {
        return PwSession.url();
    }

    public static String source() {
        return PwSession.page().content();
    }

    public static String saveSourceDump(String tag) {
        try {
            return FormStructureParser.saveHtmlToFile(PwSession.page().content(), "source-" + tag);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static void closeWebDriver() {
        PwSession.close();
    }

    public static void setWebDriver(PwDriver ignored) {
        // no-op: сессия Playwright управляется через PwSession
    }
}