package com.bft.ui.pages;

import com.bft.security.masking.SecureLogger;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.bft.enums.UIType;
import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

/**
 * Page Object для страницы авторизации в системе EVS
 * 
 * Предоставляет методы для:
 * - Открытия страницы авторизации различных окружений
 * - Стандартной авторизации (логин/пароль)
 * - Авторизации через ЕПГУ
 * - Выхода из системы
 * 
 * <p>Все методы возвращают this для поддержки fluent API (цепочки вызовов).
 * 
 * <p>Пример использования:
 * <pre>{@code
 * new LoginPage()
 *     .open(UIType.EVS_UAT_LKS)
 *     .authorize("user@test.com", "password");
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see UIType для доступных типов окружений
 * @see MainPage для работы после авторизации
 */
public class LoginPage {

    private final SecureLogger logger = SecureLogger.getLogger(getClass());

    /**
     * Открывает страницу авторизации для указанного окружения
     * 
     * Навигирует браузер на URL страницы авторизации, соответствующий
     * выбранному типу UI окружения.
     * 
     * @param uiType тип UI окружения из enum {@link UIType}
     *               (например, EVS_UAT_LKS, EVS_TEST_LKS, EVS_PROD_LKS)
     * @return текущий экземпляр LoginPage для цепочки вызовов (fluent API)
     * @see UIType для полного списка доступных окружений
     */
    public LoginPage open(UIType uiType) {

        Selenide.open(uiType.value);
        return this;
    }

    /**
     * Авторизует пользователя в системе EVS через стандартную форму
     * 
     * Выполняет вход в систему через форму авторизации:
     * 1. Вводит логин в первое поле ввода
     * 2. Вводит пароль во второе поле ввода
     * 3. Нажимает кнопку "Войти"
     * 4. Ожидает появления элемента с именем пользователя (подтверждение входа)
     * 
     * <p>Метод использует {@link SecureLogger} - пароль не логируется в открытом виде.
     * 
     * @param login логин пользователя (email или username)
     * @param password пароль пользователя
     * @return текущий экземпляр LoginPage для цепочки вызовов
     * @throws com.codeborne.selenide.ex.ElementNotFoundException 
     *         если элементы формы авторизации не найдены
     * @throws com.codeborne.selenide.ex.ElementShould 
     *         если элемент подтверждения входа не появился в течение 60 секунд
     */
    public LoginPage authorize(String login, String password) {
        logger.info("Выполняем авторизацию пользователя: {}", login);

        /*$x(String.format("//*[@id= '%s')", login)).setValue(login);*/
        /*$("input.loginInput#login").setValue(login);*/
        $x("//div[@class = 'field loginControl']//input[@class = 'loginInput']").setValue(login);
        /*$("input.loginInput#pass").setValue(password);*/
        $x("//div[@class = 'field loginControl']//input[@class = 'loginInput secondInput']").setValue(password);
        /*$("button.loginButton").click();*/
        $x("//button[contains(@class,'button is-')]").click();
        waitForLoading();
        return this;
    }

    /**
     * Авторизует пользователя через ЕПГУ (Единый портал государственных услуг)
     * 
     * Выполняет авторизацию через внешний провайдер ESIA (ЕПГУ):
     * 1. Кликает по ссылке "Войти через ЕПГУ"
     * 2. Ожидает загрузки формы ЕПГУ (до 70 секунд)
     * 3. Вводит логин и пароль
     * 4. Нажимает кнопку "Войти"
     * 
     * <p>После успешной авторизации необходимо выбрать карточку пользователя
     * через метод {@link #selectUserCardEPGU(String)}.
     * 
     * @param login логин пользователя на портале ЕПГУ
     * @param password пароль пользователя на портале ЕПГУ
     * @return текущий экземпляр LoginPage для цепочки вызовов
     * @throws com.codeborne.selenide.ex.ElementNotFoundException 
     *         если ссылка ЕПГУ или элементы формы не найдены
     * @see #selectUserCardEPGU(String) для выбора карточки после авторизации
     */
    public LoginPage authorizeEPGU(String login, String password) {
        logger.info("Выполняем авторизацию через ЕПГУ для пользователя: {}", login);

        $x("//a[starts-with(@href,'/esia')]").click();
        $("input#login").shouldBe(Condition.enabled,Duration.ofSeconds(70));
        $("input#login").setValue(login);
        $("input#password").setValue(password);
        $x("//button[contains(text(), 'Войти')]").shouldBe(Condition.enabled,Duration.ofSeconds(70)).click();
        return this;
    }

    /**
     * Выбирает карточку пользователя после авторизации через ЕПГУ
     * 
     * После авторизации через ЕПГУ система может предложить выбрать
     * одну из нескольких карточек пользователя (организация, ИП, физлицо).
     * Метод ожидает появления карточки и кликает по ней.
     * 
     * @param usercard текст на карточке пользователя для выбора
     *                 (например, "ОРГАНИЗАЦИЯ -1563384004")
     * @return текущий экземпляр LoginPage для цепочки вызовов
     * @throws com.codeborne.selenide.ex.ElementNotFoundException 
     *         если карточка с указанным текстом не найдена
     * @throws com.codeborne.selenide.ex.ElementShould 
     *         если карточка не появилась в течение 60 секунд
     */
    public LoginPage selectUserCardEPGU(String usercard) {
        $x("//*[@class = 'selectUserCardName' and text() = '" + usercard + "']").shouldBe(Condition.visible, Duration.ofSeconds(60)).click();
        return this;
    }

    /**
     * Ожидает завершения загрузки страницы после авторизации
     * 
     * Ожидает появления элемента с именем пользователя в шапке сайта,
     * что является индикатором успешной авторизации.
     */
    private void waitForLoading() {
        $x("//div[contains(@class, 'user-name')]").shouldBe(Condition.visible, Duration.ofSeconds(60));
    }

    /**
     * Выполняет выход из системы
     * 
     * Выполняет logout пользователя:
     * 1. Закрывает всплывающее сообщение об ошибке если оно есть
     * 2. Кликает по элементу с именем пользователя
     * 3. Кликает по кнопке "Выйти"
     * 4. Ожидает появления сообщения "Войдите в систему"
     * 
     * @return текущий экземпляр LoginPage для цепочки вызовов
     * @throws com.codeborne.selenide.ex.ElementShould 
     *         если сообщение "Войдите в систему" не появилось в течение 60 секунд
     */
    public LoginPage logOut() {
        if (
                $x("//div[@class= 'ps-alert-top']").isDisplayed())
        {$x("//i[@class= 'ps-icon-x ps-alert-close']").click();}

        // если сообщение об ошибке запроса сервера,закрыть ее
        $x("//div[contains(@class, 'user-name')]").click();
        $x("//button[contains(@class, 'logout')]").click();
        $x("//div[contains(text(), 'Войдите в систему')]").shouldBe(Condition.visible, Duration.ofSeconds(60));

        return this;
    }
}