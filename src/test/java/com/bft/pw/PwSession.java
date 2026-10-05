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

    /** Данные подготовки подписи, перехваченные из ответа {@code /syncPrepare} (см. {@link #awaitSigningData}). */
    private static volatile SigningData lastSigningData;

    /**
     * Информация о падении mesh-подписи, перехваченная из сбоя {@code /syncPrepare} или
     * {@code /syncSign} (не-200 статус или ответ с ошибкой). Заполняется в {@link #registerHandlers()},
     * читается и очищается через {@link #getAndClearMeshError()}. Используется, чтобы мягко провалить
     * тест подписания с внятным сообщением, а не ждать таймаут на окне выбора сертификата
     * (которое при падении mesh просто не открывается).
     */
    private static volatile String lastMeshError;

    /** true, пока активна Playwright-trace запись (evs.trace=true) */
    private static volatile boolean tracingActive = false;

    /**
     * Данные из ответа эндпоинта подготовки отчёта к подписанию ({@code /syncPrepare}).
     * Именно они нужны для завершения подписания через GraphQL/Camunda без плагина:
     * formName, itemId, xmlGuid (pre-signed file ref), requestId.
     */
    public static final class SigningData {
        public final String requestId;
        public final String itemId;
        public final String formName;
        public final String mimeType;
        public final String xmlGuid;
        public final String fileName;

        public SigningData(String requestId, String itemId, String formName,
                           String mimeType, String xmlGuid, String fileName) {
            this.requestId = requestId;
            this.itemId = itemId;
            this.formName = formName;
            this.mimeType = mimeType;
            this.xmlGuid = xmlGuid;
            this.fileName = fileName;
        }

        @Override
        public String toString() {
            return "SigningData{formName='" + formName + "', itemId='" + itemId
                    + "', xmlGuid='" + xmlGuid + "', requestId='" + requestId + "'}";
        }
    }

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

            // Подписание через КриптоПРО (evs.sign.mode=ui): Chrome (канал), headful,
            // persistent-контекст с загрузкой расширения CAdES (как в MCP-браузере).
            // Профиль переиспользуется между прогонами, чтобы «Запомнить выбор» сертификата
            // и сессия не сбрасывались.
            String uiSignMode = System.getProperty("evs.sign.mode", "");
            String extPath = System.getProperty("evs.cryptopro-extension");
            if ("ui".equalsIgnoreCase(uiSignMode)
                    && !headless && chromeLike(browserName)
                    && extPath != null && !extPath.isBlank()) {
                Path profile = Paths.get("target", "chrome-cryptopro-profile").toAbsolutePath();
                Files.createDirectories(profile);
                BrowserType.LaunchPersistentContextOptions popts =
                        new BrowserType.LaunchPersistentContextOptions()
                                .setChannel(BrowserChannel.CHROME)
                                .setHeadless(false)
                                .setViewportSize(1920, 1080)
                                .setArgs(Arrays.asList(
                                        "--disable-blink-features=AutomationControlled",
                                        "--load-extension=" + extPath,
                                        "--disable-extensions-except=" + extPath));
                context = type.launchPersistentContext(profile, popts);
                activePage = context.pages().isEmpty() ? context.newPage() : context.pages().get(0);
                registerHandlers();
                configured = true;
                log.info("Playwright (CryptoPro UI): Chrome headful + расширение {}",
                        extPath);
                return;
            }

            BrowserType.LaunchOptions opts = new BrowserType.LaunchOptions()
                    .setHeadless(headless)
                    .setArgs(Collections.singletonList("--disable-blink-features=AutomationControlled"));
            if (("chrome".equals(browserName) || "yandex".equals(browserName)) && !headless) {
                opts.setChannel(BrowserChannel.CHROME);
            }
            browser = launchWithFallback(type, opts);
            context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1920, 1080));
            activePage = context.newPage();
            registerHandlers();
            configured = true;
            log.info("Playwright browser launched: {} (headless={})",
                    browserName, headless);
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
        // Перехват подготовки подписи: фронт вызывает */syncPrepare при «Подписать и отправить»,
        // ответ содержит formName/itemId/xmlGuid/requestId для завершения подписи через API.
        activePage.onResponse(response -> {
            try {
                String url = response.url();
                boolean signingEndpoint = url != null
                        && (url.contains("/syncPrepare") || url.contains("/syncSign"));
                if (signingEndpoint && response.status() == 200) {
                    String body = response.text();
                    if (body != null && body.contains("\"itemId\"")) {
                        com.fasterxml.jackson.databind.JsonNode data =
                                new com.fasterxml.jackson.databind.ObjectMapper()
                                        .readTree(body).path("data");
                        SigningData signingData = new SigningData(
                                data.path("requestId").asText(null),
                                data.path("itemId").asText(null),
                                data.path("formName").asText(null),
                                data.path("mimeType").asText(null),
                                data.path("xmlGuid").asText(null),
                                data.path("fileName").asText(null));
                        lastSigningData = signingData;
                        log.info("Signing data captured: {}", signingData);
                    }
                }
                // Падение mesh при подписании. Ловим широкий сигнал падения инфраструктуры:
                // не-200 статус на signing/graphql-эндпоинтах, либо 200-ответ GraphQL с
                // секцией "errors". При падении mesh UI-диалог «Выбор сертификата» не
                // открывается, а в правом углу показывается тост «меш не работает».
                boolean graphqlEndpoint = url != null && url.contains("/graphql");
                String body = null;
                try {
                    body = response.text();
                } catch (Exception ignored) {
                }
                boolean graphqlErrors = graphqlEndpoint && body != null
                        && (body.contains("\"errors\"") || body.contains("Internal server error"));
                if ((signingEndpoint || graphqlEndpoint) && !(response.status() == 200)) {
                    String error = String.format(
                            "mesh-signing endpoint %s вернул HTTP %d%s",
                            url, response.status(),
                            body != null && !body.isBlank()
                                    ? ": " + (body.length() > 300 ? body.substring(0, 300) : body)
                                    : "");
                    lastMeshError = error;
                    log.warn("Перехвачено падение mesh-подписи: {}", error);
                } else if (graphqlErrors) {
                    String error = String.format(
                            "mesh GraphQL-запрос %s вернул ошибку%s",
                            url,
                            body != null && !body.isBlank()
                                    ? ": " + (body.length() > 300 ? body.substring(0, 300) : body)
                                    : "");
                    lastMeshError = error;
                    log.warn("Перехвачена mesh GraphQL-ошибка: {}", error);
                }
            } catch (Exception e) {
                log.debug("syncPrepare capture skipped: {}", e.getMessage());
            }
        });
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

    // ================= Public API =================

    /**
     * Очищает ранее перехваченные данные подписи (перед новым сценарием).
     */
    public static synchronized void resetSigningData() {
        lastSigningData = null;
    }

    /**
     * Ждёт появления данных подготовки подписи ({@code /syncPrepare}) заданное время.
     *
     * @param timeout сколько ждать
     * @return данные или {@code null} по таймауту
     */
    public static SigningData awaitSigningData(java.time.Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            SigningData data = lastSigningData;
            if (data != null) {
                return data;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return lastSigningData;
    }

    /**
     * Запускает Playwright-trace запись (скриншоты, снимки DOM, исходники).
     * Включается свойством {@code evs.trace=true}; артефакт zip прикрепляется к Allure.
     */
    public static synchronized void startTracing() {
        if (!configured || tracingActive) {
            return;
        }
        try {
            context.tracing().start(new com.microsoft.playwright.Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
            tracingActive = true;
            log.info("Playwright trace recording started");
        } catch (Exception e) {
            log.warn("Failed to start tracing: {}", e.getMessage());
        }
    }

    /**
     * Останавливает trace-запись и сохраняет zip в {@code build/reports/traces}.
     *
     * @param name имя файла (без расширения)
     * @return путь к zip или null, если запись не велась
     */
    public static Path stopTracing(String name) {
        if (!tracingActive) {
            return null;
        }
        try {
            Path dir = Paths.get("build", "reports", "traces");
            Files.createDirectories(dir);
            String safe = name.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path file = dir.resolve(safe + "-" + System.currentTimeMillis() + ".zip");
            context.tracing().stop(new com.microsoft.playwright.Tracing.StopOptions().setPath(file));
            log.info("Playwright trace saved: {}", file.toAbsolutePath());
            return file;
        } catch (Exception e) {
            log.warn("Failed to stop tracing: {}", e.getMessage());
            return null;
        } finally {
            tracingActive = false;
        }
    }

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

    /**
     * Возвращает перехваченную информацию о падении mesh-подписи (см. {@link #lastMeshError})
     * и сбрасывает её. Возвращает {@code null}, если mesh упавших ответов не было.
     *
     * @return описание падения mesh или {@code null}
     */
    public static String getAndClearMeshError() {
        String e = lastMeshError;
        lastMeshError = null;
        return e;
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
        lastMeshError = null;
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