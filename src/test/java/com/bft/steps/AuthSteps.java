package com.bft.steps;

import com.bft.enums.UIType;
import com.bft.gui.LoginPage;
import com.bft.security.CredentialManager;
import com.bft.security.TestUsers;
import com.bft.security.masking.SecureLogger;
import com.bft.ui.pages.MainPage;
import io.qameta.allure.Step;
import org.springframework.stereotype.Component;

import static com.bft.enums.TimeoutConstants.SHORT_WAIT;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;

/**
 * Шаги для авторизации, выхода и управления сессией пользователя.
 */
@Component
public class AuthSteps {

    private final SecureLogger logger = SecureLogger.getLogger(getClass());
    private final CredentialManager credentialManager = CredentialManager.getInstance();

    // Потокобезопасное хранение сессии
    private static final ThreadLocal<String> currentSessionKeyThreadLocal = ThreadLocal.withInitial(() -> null);

    private String getCurrentSessionKey() { return currentSessionKeyThreadLocal.get(); }
    private void setCurrentSessionKey(String key) { currentSessionKeyThreadLocal.set(key); }
    private void clearCurrentSessionKey() { currentSessionKeyThreadLocal.remove(); }

    @Step("Авторизация в ЕВС (ЕПГУ) на контуре {uiType} как {user}")
    public void authorizeEVS(UIType uiType, TestUsers user) {
        String sessionKey = user.getUsername() + "|" + user.getOrganization();

        if (sessionKey.equals(getCurrentSessionKey())) {
            logger.info("Сессия уже активна для {}, пропускаем авторизацию", user.getFullName());
            return;
        }

        var credentials = credentialManager.getUserCredentials(user.getCredentialPrefix());
        if (credentials == null || !credentials.isValid()) {
            throw new RuntimeException("Credentials not available for user: " + user.getFullName());
        }

        logger.info("Выполняем авторизацию для пользователя: {}", credentials.username);

        new LoginPage()
                .open(uiType)
                .authorizeEPGU(credentials.username, credentials.password)
                .selectUserCardEPGU(user.getOrganization());

        setCurrentSessionKey(sessionKey);
    }

    @Step("Выход из системы")
    public void logOut() {
        try {
            $x("//body").shouldBe(visible, SHORT_WAIT);
            new MainPage().logOut();
            logger.info("Пользователь вышел из системы");
        } catch (Exception e) {
            logger.warn("Ошибка при выходе: {}", e.getMessage());
        } finally {
            clearCurrentSessionKey();
        }
    }
}