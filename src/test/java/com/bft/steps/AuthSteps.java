package com.bft.steps;

import com.bft.enums.UIType;
import com.bft.ui.pages.LoginPage;
import com.bft.security.CredentialManager;
import com.bft.security.TestUsers;
import com.bft.security.masking.SecureLogger;
import com.bft.ui.pages.MainPage;
import com.bft.pw.PwSession;
import com.bft.pw.Selenide;
import com.microsoft.playwright.options.Cookie;
import io.qameta.allure.Step;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.bft.enums.TimeoutConstants.SHORT_WAIT;
import static com.bft.pw.Condition.visible;
import static com.bft.pw.Selenide.$x;

/**
 * Шаги для авторизации, выхода и управления сессией пользователя.
 */
public class AuthSteps {

    private final SecureLogger logger = SecureLogger.getLogger(getClass());
    private final CredentialManager credentialManager = CredentialManager.getInstance();

    /** Ключ текущей сессии (username|organization) для пропуска повторной авторизации */
    private static volatile String currentSessionKey = null;

    /**
     * Авторизация в системе ЕВС через ЕПГУ для архивной организации.
     * Контур определяется автоматически из {@code evs.ui.type} через {@link com.bft.enums.UITypeSelector}.
     * Использует учётные данные пользователя {@code evs.user2} (Бездомный Иван).
     *
     * @throws RuntimeException если учетные данные evs.user2 недоступны
     */
    @Step(value = "Авторизация в ЕВС (ЕПГУ)")
    public void authorizeArhivEVS() {
        authorizeArhivEVS(com.bft.enums.UITypeSelector.getSelectedUIType());
    }

    /**
     * Авторизация в системе ЕВС через ЕПГУ для архивной организации с указанием контура.
     * Использует учётные данные пользователя {@code evs.user1} (Кривоносов)
     * и организацию {@code evs.user1.organization} (ОРГАНИЗАЦИЯ -1546025669),
     * под которой доступен «ЛК Архивной организации».
     *
     * @param uiType тип UI окружения (EVS_TEST_LKS, EVS_UAT_LKS и т.д.)
     * @throws RuntimeException если учетные данные evs.user1 недоступны
     */
    @Step(value = "Авторизация в ЕВС (ЕПГУ) на контуре {uiType}")
    public void authorizeArhivEVS(UIType uiType) {
        var credentials = credentialManager.getUserCredentials("evs.user1");
        if (credentials == null || !credentials.isValid()) {
            throw new RuntimeException("Archive user credentials not available. Please set evs.user1.username and evs.user1.password environment variables.");
        }

        String organization = credentialManager.getCredential("evs.user1.organization", "ОРГАНИЗАЦИЯ -1546025669");
        logger.info("Выполняем авторизацию в ЕВС через ЕПГУ для пользователя: {}", credentials.username);

        new LoginPage()
                .open(uiType)
                .authorizeEPGU(credentials.username, credentials.password)
                .selectUserCardEPGU(organization);
    }

    /**
     * Авторизация в системе РПУ (Региональный портал услуг).
     *
     * @param uiType тип UI окружения (UAT, TEST, PROD)
     * @throws RuntimeException если учетные данные RPU недоступны
     */
    @Step("Авторизация в РПУ")
    public void authorizeArhivRPU(UIType uiType) {
        var credentials = credentialManager.getUserCredentials("rpu");
        if (credentials == null || !credentials.isValid()) {
            throw new RuntimeException("RPU credentials not available. Please set rpu.username and rpu.password environment variables.");
        }

        logger.info("Выполняем авторизацию в РПУ для пользователя: {}", credentials.username);

        new LoginPage()
                .open(uiType)
                .authorize(credentials.username, credentials.password);
    }

    @Step("Авторизация в ЕВС (ЕПГУ) на контуре {uiType} как {user}")
    public void authorizeEVS(UIType uiType, TestUsers user) {
        logger.info("Выполняем авторизацию в EVS для пользователя: {}", user.getFullName());

        if (!user.hasCredentials()) {
            throw new IllegalStateException(
                String.format("Credentials для пользователя '%s' недоступны. " +
                    "Проверьте файл credentials.properties или переменные окружения.",
                    user.getFullName())
            );
        }

        performEpguAuth(uiType, user.getUsername(), user.getPassword(), user.getOrganization());
    }

    @Step(value = "Авторизация в EVS (дефолтный пользователь)")
    public void authorizeEVS(UIType uiType) {
        var credentials = credentialManager.getUserCredentials("evs");

        if (credentials == null || !credentials.isValid()) {
            logger.warn("EVS credentials не найдены через CredentialManager, используем DEFAULT_USER из TestUsers");
            authorizeEVS(uiType, TestUsers.DEFAULT_USER);
            return;
        }

        String organization = credentialManager.getCredential("evs.organization", "ОРГАНИЗАЦИЯ -1546025669");

        performEpguAuth(uiType, credentials.username, credentials.password, organization);
    }

    @Step("Выход с учетной записи")
    public void logOut() {
        $x("//body").shouldBe(visible, SHORT_WAIT);
        new MainPage()
                .logOut();
        currentSessionKey = null;
    }

    /**
     * Проверяет, авторизован ли пользователь в текущей сессии браузера.
     * Ожидает загрузки страницы (логин или дашборд), затем проверяет наличие user-name.
     */
    private boolean isUserLoggedIn() {
        try {
            Selenide.Wait().withTimeout(java.time.Duration.ofSeconds(10)).until(webDriver -> {
                boolean onLoginPage = !webDriver.findElements(
                    com.bft.pw.By.xpath("//a[starts-with(@href,'/esia')]")).isEmpty();
                boolean onDashboard = !webDriver.findElements(
                    com.bft.pw.By.xpath("//div[contains(@class, 'user-name')]")).isEmpty();
                return onLoginPage || onDashboard;
            });
            return $x("//div[contains(@class, 'user-name')]").isDisplayed();
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Выполняет авторизацию через ЕПГУ, пропуская если уже авторизован под нужной учёткой.
     * При авторизации под другой учёткой — сначала разлогинивается.
     */
    private void performEpguAuth(UIType uiType, String username, String password, String organization) {
        String sessionKey = username + "|" + organization;

        // 1. Профиль локального фронта (application-auth-dev.yml) — только если cookie ещё живой.
        if (AuthBypassConfig.isEnabled() && authorizeEVSBypass(uiType)) {
            currentSessionKey = "bypass|" + sessionKey;
            return;
        }

        // 2. Кэш live-сессии прошлых успешных прогонов — ESIA не трогаем.
        if (tryAuthorizeFromSessionCache(uiType)) {
            currentSessionKey = "cache|" + sessionKey;
            return;
        }

        // 3. Реальный ESIA-логин с захватом свежей сессии в кэш.
        new LoginPage().open(uiType);

        if (isUserLoggedIn() && sessionKey.equals(currentSessionKey)) {
            logger.info("Уже авторизован как '{}', пропускаем повторную авторизацию", organization);
            return;
        }

        if (isUserLoggedIn()) {
            logger.info("Авторизован под другой учёткой, выполняем logout");
            new LoginPage().logOut();
            new LoginPage().open(uiType);
        }

        new LoginPage()
                .authorizeEPGU(username, password)
                .selectUserCardEPGU(organization)
                .open(uiType);

        currentSessionKey = sessionKey;
        cacheCurrentSession();
    }

    /**
     * Обход ESIA: подставляет в браузерный контекст session-cookie oauth2_proxy из локального
     * профиля фронта (application-auth-dev.yml: ps.auth.cookie + ps.auth.user-id), как это делает
     * фронт при локальном запуске. Реальный ESIA-логин не выполняется, что устраняет флаковость
     * внешнего контура sso-test.sfr.gov.ru/esia/callback.
     */
    @Step(value = "Авторизация в EVS (обход ESIA через cookie, как на фронте локально)")
    public boolean authorizeEVSBypass(UIType uiType) {
        String base = uiType.value;
        logger.info("Обход ESIA: подставляем session-cookie из профиля локального фронта");

        String rawCookie = AuthBypassConfig.cookieValue();
        if (rawCookie == null || rawCookie.isEmpty()) {
            logger.warn("Bypass: cookie профиля фронта не задан");
            return false;
        }

        // Значение ps.auth.cookie из application-auth-dev.yml — это строка из нескольких cookie
        // (формат заголовка Set-Cookie), например: <val0>; _oauth2_proxy_1=<val1>,
        // где <val0> — значение для _oauth2_proxy/_oauth2_proxy_0 (в dev-профиле без префикса
        // имени), а _oauth2_proxy_1 — отдельное. Парсим в пары имя->значение.
        Map<String, String> parsed = new LinkedHashMap<>();
        String bareValue = null;
        for (String part : rawCookie.split(";")) {
            String p = part.trim();
            if (p.isEmpty()) {
                continue;
            }
            int eq = p.indexOf('=');
            if (eq > 0) {
                parsed.put(p.substring(0, eq), p.substring(eq + 1));
            } else {
                bareValue = p;
            }
        }
        String mainName = AuthBypassConfig.cookieName(); // _oauth2_proxy
        String baseVal = bareValue != null ? bareValue : parsed.get(mainName);
        if (baseVal == null || baseVal.isEmpty()) {
            logger.warn("Bypass: не удалось извлечь значение cookie из профиля фронта");
            return false;
        }
        String v0 = parsed.getOrDefault("_oauth2_proxy_0", baseVal);
        String v1 = parsed.getOrDefault("_oauth2_proxy_1", baseVal);

        Map<String, String> session = new LinkedHashMap<>();
        session.put(mainName, baseVal);
        session.put("_oauth2_proxy_0", v0);
        session.put("_oauth2_proxy_1", v1);
        if (!AuthSessionCache.isFresh(session)) {
            logger.warn("Bypass: cookie профиля фронта протух — ищем свежую сессию в кэше/через ESIA");
            return false;
        }

        com.bft.pw.Selenide.open(base);
        String domain = AuthBypassConfig.domainFor(base);
        List<Cookie> cookies = new ArrayList<>();
        cookies.add(bypassCookie(mainName, baseVal, domain));
        cookies.add(bypassCookie("_oauth2_proxy_0", v0, domain));
        cookies.add(bypassCookie("_oauth2_proxy_1", v1, domain));
        PwSession.context().addCookies(cookies);

        if (userId() != null && !userId().isEmpty()) {
            PwSession.context().setExtraHTTPHeaders(Map.of("x-auth-request-user", userId()));
        }

        com.bft.pw.Selenide.open(base);
        return looksAuthenticated(base);
    }

    /**
     * Пытается подставить кэшированную live-сессию прошлого успешного прогона.
     */
    private boolean tryAuthorizeFromSessionCache(UIType uiType) {
        try {
            String base = uiType.value;
            Map<String, String> cached = AuthSessionCache.load();
            if (cached == null) {
                return false;
            }
            if (!AuthSessionCache.isFresh(cached)) {
                logger.warn("Кэш сессии протух, требуется реальный ESIA-логин");
                return false;
            }
            com.bft.pw.Selenide.open(base);
            String domain = AuthBypassConfig.domainFor(base);
            List<Cookie> cookies = new ArrayList<>();
            for (Map.Entry<String, String> e : cached.entrySet()) {
                if (e.getKey().contains("_oauth2_proxy")) {
                    cookies.add(bypassCookie(e.getKey(), e.getValue(), domain));
                }
            }
            PwSession.context().addCookies(cookies);
            com.bft.pw.Selenide.open(base);
            if (looksAuthenticated(base)) {
                logger.info("Авторизован по кэшированной сессии");
                return true;
            }
            logger.warn("Кэш сессии не был принят сервером");
            return false;
        } catch (Exception e) {
            logger.warn("Не удалось авторизоваться по кэшу сессии: " + e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет, что после open(base) мы не ушли на SSO-логин (sso-test) и не открыли форму входа.
     */
    private boolean looksAuthenticated(String base) {
        try {
            String url = PwSession.page().url();
            if (url != null && (url.contains("sso-test") || url.contains("oauth2/authorize"))) {
                return false;
            }
            String content = PwSession.page().content();
            return content == null || (!content.contains("oauth2/authorize") && !content.contains("Вход в систему"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Сохраняет live-сессию (oauth2_proxy cookie) после успешного реального ESIA-логина.
     */
    private void cacheCurrentSession() {
        try {
            Map<String, String> cookies = new LinkedHashMap<>();
            for (Cookie c : PwSession.context().cookies()) {
                if (c.name.contains("_oauth2_proxy")) {
                    cookies.put(c.name, c.value);
                }
            }
            if (cookies.isEmpty()) {
                logger.warn("После логина не нашлись cookie _oauth2_proxy — сессия не кэширована");
                return;
            }
            AuthSessionCache.save(cookies);
            logger.info("Live-сессия сохранена в кэш (" + cookies.size() + " cookie)");
        } catch (Exception e) {
            logger.warn("Не удалось кэшировать сессию: " + e.getMessage());
        }
    }

    private String userId() {
        return AuthBypassConfig.userId();
    }

    private static Cookie bypassCookie(String name, String value, String domain) {
        return new Cookie(name, value)
                .setDomain(domain)
                .setPath("/")
                .setSecure(true)
                .setHttpOnly(true);
    }
}