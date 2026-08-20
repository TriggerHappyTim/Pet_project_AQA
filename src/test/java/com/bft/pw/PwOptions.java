package com.bft.pw;

import com.bft.pw.logging.LogEntries;
import com.bft.pw.logging.LogType;
import com.bft.pw.logging.PwLogs;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;

import java.util.List;

/**
 * Имитация {@code WebDriver.Options}.
 */
public class PwOptions {

    private final Page page;

    public PwOptions(Page page) {
        this.page = page;
    }

    public Window window() {
        return new Window(page);
    }

    public PwLogs logs() {
        return new PwLogs();
    }

    public void deleteAllCookies() {
        PwSession.context().clearCookies();
    }

    public void deleteCookieNamed(String name) {
        PwSession.context().clearCookies(
                new com.microsoft.playwright.BrowserContext.ClearCookiesOptions()
                        .setName(name));
    }

    public void addCookie(String name, String value) {
        PwSession.context().addCookies(
                java.util.Collections.singletonList(new Cookie(name, value)
                        .setUrl(page.url())));
    }

    public static final class Window {
        private final Page page;

        public Window(Page page) {
            this.page = page;
        }

        public Window maximize() {
            page.setViewportSize(1920, 1080);
            return this;
        }

        public Window fullscreen() {
            page.setViewportSize(1920, 1080);
            return this;
        }

        public Window setSize(int width, int height) {
            page.setViewportSize(width, height);
            return this;
        }
    }
}