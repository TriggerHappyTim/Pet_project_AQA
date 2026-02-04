package com.bft.test.helpers;

import com.bft.enums.UIType;
import com.bft.enums.UITypeSelector;
import com.bft.security.CredentialManager;
import com.bft.ui.pages.LoginPage;
import com.codeborne.selenide.Selenide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Вспомогательный класс для упрощения авторизации в тестах
 * 
 * <p>Предоставляет методы для:
 * - Стандартной авторизации с использованием CredentialManager
 * - Авторизации через ЕПГУ
 * - Авторизации с кастомными учетными данными
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Стандартная авторизация
 * LoginHelper.loginAsEVSUser(UIType.EVS_UAT_LKS);
 * 
 * // Авторизация через ЕПГУ
 * LoginHelper.loginViaEPGU(UIType.EVS_UAT_LKS, "user@example.com", "password");
 * 
 * // Авторизация с кастомными данными
 * LoginHelper.loginWithCredentials(UIType.EVS_UAT_LKS, "custom@user.com", "pass123");
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 1.0
 * @since 1.0
 */
public class LoginHelper {

    private static final Logger logger = LoggerFactory.getLogger(LoginHelper.class);

    /**
     * Выполняет стандартную авторизацию пользователя EVS
     * 
     * Использует учетные данные из CredentialManager для указанного окружения.
     * 
     * @param uiType тип UI окружения
     * @return экземпляр LoginPage после авторизации
     * @throws RuntimeException если учетные данные не найдены или авторизация не удалась
     */
    public static LoginPage loginAsEVSUser(UIType uiType) {
        logger.info("Авторизация пользователя EVS в окружении: {}", uiType);
        
        var credentials = CredentialManager.getInstance().getUserCredentials("evs");
        if (credentials == null || !credentials.isValid()) {
            throw new RuntimeException(
                "Не удалось получить валидные учетные данные для EVS. " +
                "Проверьте настройки CredentialManager."
            );
        }
        
        return loginWithCredentials(uiType, credentials.username, credentials.password);
    }

    /**
     * Выполняет авторизацию с указанными учетными данными
     * 
     * @param uiType тип UI окружения
     * @param username логин пользователя
     * @param password пароль пользователя
     * @return экземпляр LoginPage после авторизации
     */
    public static LoginPage loginWithCredentials(UIType uiType, String username, String password) {
        logger.info("Авторизация пользователя {} в окружении: {}", username, uiType);
        
        return new LoginPage()
            .open(uiType)
            .authorize(username, password);
    }

    /**
     * Выполняет авторизацию через ЕПГУ с указанными учетными данными
     * 
     * @param uiType тип UI окружения
     * @param username логин пользователя на портале ЕПГУ
     * @param password пароль пользователя на портале ЕПГУ
     * @return экземпляр LoginPage после авторизации через ЕПГУ
     */
    public static LoginPage loginViaEPGU(UIType uiType, String username, String password) {
        logger.info("Авторизация через ЕПГУ пользователя {} в окружении: {}", username, uiType);
        
        return new LoginPage()
            .open(uiType)
            .authorizeEPGU(username, password);
    }

    /**
     * Выполняет авторизацию через ЕПГУ с использованием CredentialManager
     * 
     * Использует учетные данные из CredentialManager для ЕПГУ.
     * 
     * @param uiType тип UI окружения
     * @param userCard карточка пользователя для выбора после авторизации
     * @return экземпляр LoginPage после авторизации и выбора карточки
     * @throws RuntimeException если учетные данные не найдены
     */
    public static LoginPage loginViaEPGUWithUserCard(UIType uiType, String userCard) {
        logger.info("Авторизация через ЕПГУ с выбором карточки: {} в окружении: {}", userCard, uiType);
        
        var credentials = CredentialManager.getInstance().getUserCredentials("evs");
        if (credentials == null || !credentials.isValid()) {
            throw new RuntimeException(
                "Не удалось получить валидные учетные данные для ЕПГУ. " +
                "Проверьте настройки CredentialManager."
            );
        }
        
        return new LoginPage()
            .open(uiType)
            .authorizeEPGU(credentials.username, credentials.password)
            .selectUserCardEPGU(userCard);
    }

    /**
     * Проверяет, что пользователь авторизован
     * 
     * Проверяет наличие элемента с именем пользователя на странице.
     * 
     * @return true если пользователь авторизован, false в противном случае
     */
    public static boolean isUserLoggedIn() {
        try {
            return com.codeborne.selenide.Selenide.$x("//div[contains(@class, 'user-name')]")
                .isDisplayed();
        } catch (Exception e) {
            logger.debug("Пользователь не авторизован: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Выполняет выход из системы
     * 
     * Использует стандартный метод logout из LoginPage или MainPage.
     */
    public static void logout() {
        logger.info("Выход из системы");
        try {
            // Попытка выхода через кнопку выхода
            com.codeborne.selenide.Selenide.$x("//a[contains(text(), 'Выход')]")
                .click();
            
            // Ожидание завершения выхода
            SmartWaits.waitForPageLoad();
        } catch (Exception e) {
            logger.warn("Не удалось выполнить выход стандартным способом: {}", e.getMessage());
            // Альтернативный способ - открыть страницу логина напрямую
            Selenide.open(UITypeSelector.getSelectedUIType().value);
        }
    }
}
