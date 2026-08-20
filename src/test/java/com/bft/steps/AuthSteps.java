package com.bft.steps;

import com.bft.enums.UIType;
import com.bft.ui.pages.LoginPage;
import com.bft.security.CredentialManager;
import com.bft.security.TestUsers;
import com.bft.security.masking.SecureLogger;
import com.bft.ui.pages.MainPage;
import com.bft.pw.Selenide;
import io.qameta.allure.Step;
import org.springframework.stereotype.Component;

import static com.bft.enums.TimeoutConstants.SHORT_WAIT;
import static com.bft.pw.Condition.visible;
import static com.bft.pw.Selenide.$x;

/**
 * Шаги для авторизации, выхода и управления сессией пользователя.
 */
@Component
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
     * Использует учётные данные пользователя {@code evs.user2} (Бездомный Иван)
     * и организацию из {@code evs.user2.organization}.
     *
     * @param uiType тип UI окружения (EVS_TEST_LKS, EVS_UAT_LKS и т.д.)
     * @throws RuntimeException если учетные данные evs.user2 недоступны
     */
    @Step(value = "Авторизация в ЕВС (ЕПГУ) на контуре {uiType}")
    public void authorizeArhivEVS(UIType uiType) {
        var credentials = credentialManager.getUserCredentials("evs.user2");
        if (credentials == null || !credentials.isValid()) {
            throw new RuntimeException("Archive user credentials not available. Please set evs.user2.username and evs.user2.password environment variables.");
        }

        String organization = credentialManager.getCredential("evs.user2.organization", "ОРГАНИЗАЦИЯ -1546025669");
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
    }
}