package com.bft.pw;

import com.bft.config.TestConfig;
import com.bft.pw.logging.LogEntry;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.BrowserChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Управление жизненным циклом Playwright-сессии.
 * Заменяет Selenide WebDriverRunner/Configuration.
 */
public final class PwSession {

    private static final Logger log = LoggerFactory.getLogger(PwSession.class);

    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page activePage;
    private static volatile boolean configured = false;

    private static final List<LogEntry> consoleLog = Collections.synchronizedList(new ArrayList<>());
    private static volatile Dialog lastDialog;

    private PwSession() {
    }

    /**
     * Единократная настройка Playwright-сессии (браузер, контекст, страница, расширение).
     */
    public static synchronized void configure(TestConfig config) {
        if (configured) {
            return;
        }
        String browserName = config.getBrowser().toLowerCase();
        boolean headless = config.isHeadless();
        try {
            playwright = Playwright.create();
            BrowserType type = selectBrowserType(browserName);

            Path extDir = chromeLike(browserName) ? unpackExtension(config.getCryptoProPath()) : null;
            if (!headless && extDir != null && "chrome".equals(browserName)) {
                // Расширения Chrome поддерживаются только в headed-режиме с системным Chrome
                Path profile = Files.createTempDirectory("pw-profile");
                BrowserType.LaunchPersistentContextOptions opts = new BrowserType.LaunchPersistentContextOptions()
                        .setChannel("chrome")
                        .setHeadless(false)
                        .setViewportSize(1920, 1080)
                        .setArgs(Arrays.asList(
                                "--disable-blink-features=AutomationControlled",
                                "--disable-extensions-except=" + extDir.toAbsolutePath(),
                                "--load-extension=" + extDir.toAbsolutePath()));
                context = type.launchPersistentContext(profile, opts);
                activePage = context.pages().isEmpty() ? context.newPage() : context.pages().get(0);
            } else {
                BrowserType.LaunchOptions opts = new BrowserType.LaunchOptions()
                        .setHeadless(headless)
                        .setArgs(Collections.singletonList("--disable-blink-features=AutomationControlled"));
                if (("chrome".equals(browserName) || "yandex".equals(browserName)) && !headless) {
                    opts.setChannel(BrowserChannel.CHROME);
                }
                browser = launchWithFallback(type, opts);
                context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1920, 1080));
                activePage = context.newPage();
            }
            registerHandlers();
            configured = true;
            log.info("Playwright browser launched: {} (headless={}, extension={})",
                    browserName, headless, extDir != null);
        } catch (Exception e) {
            log.error("Failed to launch Playwright browser '{}'", browserName, e);
            throw new IllegalStateException("Failed to launch Playwright browser: " + browserName, e);
        }
    }

    private static Browser launchWithFallback(BrowserType type, BrowserType.LaunchOptions opts) {
        try {
            return type.launch(opts);
        } catch (Exception e) {
            log.warn("Launch with channel failed, fallback to bundled browser: {}", e.getMessage());
            BrowserType.LaunchOptions fallback = new BrowserType.LaunchOptions()
                    .setHeadless(opts.headless)
                    .setArgs(opts.args);
            return type.launch(fallback);
        }
    }

    private static void registerHandlers() {
        context.onPage(p -> {
            p.onConsoleMessage(msg -> consoleLog.add(new LogEntry(msg.text())));
            p.onDialog(dialog -> {
                lastDialog = dialog;
            });
        });
        activePage.onConsoleMessage(msg -> consoleLog.add(new LogEntry(msg.text())));
        activePage.onDialog(dialog -> lastDialog = dialog);
    }

    private static BrowserType selectBrowserType(String browserName) {
        if ("firefox".equalsIgnoreCase(browserName)) {
            return playwright.firefox();
        }
        if ("webkit".equalsIgnoreCase(browserName)) {
            return playwright.webkit();
        }
        return playwright.chromium();
    }

    private static boolean chromeLike(String browserName) {
        return "chrome".equalsIgnoreCase(browserName)
                || "yandex".equalsIgnoreCase(browserName)
                || "edge".equalsIgnoreCase(browserName);
    }

    /**
     * Распаковывает CRX-расширение (zip с заголовком) или принимает каталог расширения.
     */
    private static Path unpackExtension(String path) {
        if (path == null || path.isEmpty()) {
            log.warn("CryptoPro extension path is empty, tests may fail on certificate dialog");
            return null;
        }
        try {
            Path file = Paths.get(path);
            if (!Files.exists(file)) {
                log.warn("CryptoPro extension not found at '{}'", path);
                return null;
            }
            if (Files.isDirectory(file)) {
                return file;
            }
            byte[] data = Files.readAllBytes(file);
            int zipStart = indexOf(data, new byte[]{'P', 'K', 3, 4});
            if (zipStart < 0) {
                log.warn("CRX '{}' does not look like a zip archive", path);
                return null;
            }
            Path out = Files.createTempDirectory("cryptopro-ext");
            try (ZipInputStream zis = new ZipInputStream(
                    new ByteArrayInputStream(data, zipStart, data.length - zipStart))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path target = out.resolve(entry.getName()).normalize();
                    if (!target.startsWith(out)) {
                        continue;
                    }
                    if (entry.isDirectory()) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
            log.info("CryptoPro extension unpacked to {}", out.toAbsolutePath());
            return out;
        } catch (IOException e) {
            log.warn("Failed to unpack CryptoPro extension: {}", e.getMessage());
            return null;
        }
    }

    private static int indexOf(byte[] haystack, byte[] needle) {
        outer:
        for (int i = 0; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    // ================= Public API =================

    public static Page page() {
        ensureConfigured();
        return activePage;
    }

    public static BrowserContext context() {
        ensureConfigured();
        return context;
    }

    public static void setActivePage(Page p) {
        activePage = p;
    }

    public static PwDriver driver() {
        return new PwDriver(page());
    }

    public static String url() {
        return configured ? activePage.url() : "";
    }

    public static Object executeJavaScript(String script, Object... args) {
        Object[] mapped = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof PwElement) {
                mapped[i] = ((PwElement) args[i]).elementHandle();
            } else {
                mapped[i] = args[i];
            }
        }
        return evaluate(page(), script, mapped);
    }

    /**
     * Выполняет Selenium-стилевой скрипт (с {@code return} и {@code arguments[N]}) через Playwright.
     * Playwright Java трактует строку, начинающуюся не с {@code function}, как выражение,
     * поэтому скрипт оборачивается в тело функции, а аргументы передаются единым JS-массивом.
     */
    static Object evaluate(Page page, String script, Object... mapped) {
        String body = script;
        for (int i = 0; i < mapped.length; i++) {
            body = body.replace("arguments[" + i + "]", "__pwArg" + i);
        }
        StringBuilder sb = new StringBuilder("function() { ");
        for (int i = 0; i < mapped.length; i++) {
            sb.append("var __pwArg").append(i)
                    .append(" = arguments[0] != null ? arguments[0][").append(i).append("] : null; ");
        }
        sb.append(body).append("\n}");
        if (mapped.length == 0) {
            return page.evaluate(sb.toString());
        }
        return page.evaluate(sb.toString(), Arrays.asList(mapped));
    }

    public static void confirm() {
        if (lastDialog != null) {
            lastDialog.accept();
            lastDialog = null;
        }
    }

    public static void dismiss() {
        if (lastDialog != null) {
            lastDialog.dismiss();
            lastDialog = null;
        }
    }

    public static Dialog lastDialog() {
        return lastDialog;
    }

    public static List<LogEntry> consoleLogs() {
        return new ArrayList<>(consoleLog);
    }

    public static void sleep(long millis) {
        page().waitForTimeout(millis);
    }

    public static String screenshot(String name) {
        try {
            String dir = "build/reports/screenshots";
            Files.createDirectories(Paths.get(dir));
            String safe = name.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path path = Paths.get(dir, safe + ".png");
            page().screenshot(new Page.ScreenshotOptions().setPath(path));
            return path.toAbsolutePath().toString();
        } catch (Exception e) {
            log.warn("Failed to take screenshot '{}': {}", name, e.getMessage());
            return null;
        }
    }

    public static synchronized void close() {
        if (!configured) {
            return;
        }
        try {
            if (context != null) {
                context.close();
            }
        } catch (Exception e) {
            log.debug("Context close error (ignored): {}", e.getMessage());
        }
        try {
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception e) {
            log.debug("Playwright close error (ignored): {}", e.getMessage());
        }
        playwright = null;
        browser = null;
        context = null;
        activePage = null;
        configured = false;
        consoleLog.clear();
        lastDialog = null;
    }

    public static boolean isConfigured() {
        return configured;
    }

    private static void ensureConfigured() {
        if (!configured) {
            throw new IllegalStateException(
                    "Playwright session is not configured. Call PwSession.configure(new TestConfig()) first.");
        }
    }

    // InputStream wrapper to avoid unused import warning
    static InputStream wrap(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    static String toUtf8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}