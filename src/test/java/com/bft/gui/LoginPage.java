package com.bft.gui;

import com.bft.enums.TimeoutConstants;
import com.bft.enums.UIType;
import com.bft.security.masking.SecureLogger;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.By;

import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

/**
 * Страница авторизации.
 * Рефакторинг: устойчивые локаторы, декомпозиция логики ЕПГУ, надежные ожидания.
 */
public class LoginPage {

    private final SecureLogger logger = SecureLogger.getLogger(getClass());

    // === Локаторы (вынесены в константы для удобства поддержки) ===
    private static final String LOGIN_INPUT_XPATH = "//div[contains(@class, 'loginControl')]//input[@class='loginInput'][1]";
    private static final String PASSWORD_INPUT_XPATH = "//div[contains(@class, 'loginControl')]//input[@class='loginInput'][2]";
    private static final String LOGIN_BUTTON_XPATH = "//button[contains(@class, 'loginButton') or contains(@class, 'is-primary')]";
    private static final String USER_NAME_BLOCK_XPATH = "//div[contains(@class, 'user-name')]";
    private static final String LOGOUT_BUTTON_XPATH = "//button[contains(@class, 'logout')]";
    private static final String EP_GU_LINK_XPATH = "//a[contains(@href, '/esia') or contains(text(), 'ЕПГУ')]";
    private static final String CARD_SELECT_XPATH = "//*[@class='selectUserCardName']";
    private static final String ALERT_CLOSE_XPATH = "//i[contains(@class, 'ps-icon-x') and contains(@class, 'ps-alert-close')]";

    /**
     * Открывает страницу авторизации по типу окружения.
     */
    public LoginPage open(UIType uiType) {
        logger.info("Открытие страницы авторизации: {}", uiType);
        Selenide.open(uiType.value);
        waitForPageStability();
        return this;
    }

    /**
     * Открывает страницу авторизации по прямому URL (для совместимости).
     */
    public LoginPage open(String url) {
        logger.info("Открытие страницы авторизации по URL: {}", url);
        Selenide.open(url);
        waitForPageStability();
        return this;
    }

    /**
     * Стандартная авторизация (Логин/Пароль).
     */
    public LoginPage authorize(String login, String password) {
        logger.info("Выполнение стандартной авторизации для пользователя: {}", login);

        $(By.xpath(LOGIN_INPUT_XPATH)).shouldBe(visible, TimeoutConstants.DEFAULT_WAIT).setValue(login);
        $(By.xpath(PASSWORD_INPUT_XPATH)).shouldBe(visible, TimeoutConstants.DEFAULT_WAIT).setValue(password);
        $(By.xpath(LOGIN_BUTTON_XPATH)).shouldBe(enabled, TimeoutConstants.DEFAULT_WAIT).click();

        waitForUserLoaded();
        logger.info("Авторизация успешна");
        return this;
    }

    /**
     * Авторизация через ЕПГУ (Госуслуги).
     * Разбита на этапы: клик -> ввод данных -> выбор карточки.
     */
    public LoginPage authorizeEPGU(String login, String password) {
        logger.info("Начало авторизации через ЕПГУ для: {}", login);

        // 1. Клик по кнопке ЕПГУ
        $(By.xpath(EP_GU_LINK_XPATH)).shouldBe(visible, TimeoutConstants.LONG_WAIT).click();

        // 2. Ввод данных на стороне Госуслуг
        SelenideElement loginField = $("#login");
        SelenideElement passField = $("#password");
        SelenideElement submitBtn = $x("//button[contains(text(), 'Войти')]");

        loginField.shouldBe(visible, TimeoutConstants.EPGU_AUTH_WAIT).setValue(login);
        passField.shouldBe(visible, TimeoutConstants.DEFAULT_WAIT).setValue(password);
        submitBtn.shouldBe(enabled, TimeoutConstants.DEFAULT_WAIT).click();

        // 3. Ожидание редиректа и выбора карточки
        waitForEsguRedirect();

        // Если появилась страница выбора карточки — выбираем её
        if ($$(By.xpath(CARD_SELECT_XPATH)).size() > 0) {
            logger.info("Обнаружен выбор карточки, переходим к селекции...");
            selectUserCardFromList(login);
        } else {
            logger.info("Выбор карточки не требуется (одна карта или авто-вход)");
        }

        waitForUserLoaded();
        logger.info("Авторизация через ЕПГУ завершена успешно");
        return this;
    }

    /**
     * Явный выбор карточки (если метод авторизации не вызвал его автоматически).
     */
    public LoginPage selectUserCardEPGU(String userCard) {
        logger.info("Ручной выбор карточки: {}", userCard);
        waitForEsguRedirect();
        selectUserCardFromList(userCard);
        waitForUserLoaded();
        return this;
    }

    /**
     * Выход из системы.
     */
    public LoginPage logOut() {
        logger.info("Выполнение выхода из системы");

        try {
            // Закрыть алерт если есть
            if ($(By.xpath(ALERT_CLOSE_XPATH)).exists()) {
                $(By.xpath(ALERT_CLOSE_XPATH)).click();
            }

            // Меню пользователя -> Выход
            $(By.xpath(USER_NAME_BLOCK_XPATH)).shouldBe(visible, TimeoutConstants.DEFAULT_WAIT).click();
            $(By.xpath(LOGOUT_BUTTON_XPATH)).shouldBe(visible, TimeoutConstants.DEFAULT_WAIT).click();

            // Ожидание экрана логина
            $(By.xpath(LOGIN_INPUT_XPATH)).shouldBe(visible, TimeoutConstants.LONG_WAIT);
            logger.info("Выход выполнен успешно");
        } catch (Exception e) {
            logger.warn("При выходе произошла ошибка (возможно сессия уже истекла): {}", e.getMessage());
        }
        return this;
    }

    // === Приватные методы помощи (Helpers) ===

    /**
     * Ожидание стабильности страницы после открытия.
     */
    private void waitForPageStability() {
        $(By.xpath(LOGIN_INPUT_XPATH)).shouldBe(exist, TimeoutConstants.PAGE_LOAD_WAIT);
    }

    /**
     * Ожидание завершения редиректа от ЕПГУ.
     */
    private void waitForEsguRedirect() {
        logger.debug("Ожидание завершения редиректа ЕПГУ...");
        Wait().until(driver -> {
            String url = WebDriverRunner.url();
            boolean isRedirectFinished = !url.contains("/esia") && !url.contains("auth.");
            boolean isCardSelectionVisible = $$(By.xpath(CARD_SELECT_XPATH)).size() > 0;
            boolean isUserLoggedIn = $(By.xpath(USER_NAME_BLOCK_XPATH)).exists();

            return isRedirectFinished || isCardSelectionVisible || isUserLoggedIn;
        });
    }

    /**
     * Логика выбора карточки из списка с несколькими стратегиями поиска.
     */
    private void selectUserCardFromList(String userCard) {
        String normalizedCard = userCard.trim();
        // Извлекаем номер организации (последние цифры)
        String orgNumber = normalizedCard.replaceAll(".*?(-?\\d+)$", "$1");

        logger.debug("Поиск карточки. Полный текст: '{}', Номер_org: '{}'", normalizedCard, orgNumber);

        ElementsCollection cards = $$(By.xpath(CARD_SELECT_XPATH));
        if (cards.isEmpty()) {
            throw new RuntimeException("Список карточек пуст. Авторизация невозможна.");
        }

        boolean found = false;
        for (SelenideElement card : cards) {
            String text = card.getText().trim();

            // Стратегия 1: По номеру организации (самая надежная)
            if (text.contains(orgNumber)) {
                logger.info("Карточка найдена по номеру организации: {}", text);
                card.scrollTo().shouldBe(visible).click();
                found = true;
                break;
            }

            // Стратегия 2: Частичное совпадение текста
            if (text.toLowerCase().contains(normalizedCard.toLowerCase())) {
                logger.info("Карточка найдена по тексту: {}", text);
                card.scrollTo().shouldBe(visible).click();
                found = true;
                break;
            }
        }

        if (!found) {
            // Собираем список доступных карточек для лога ошибки (используем Collectors для Java 11)
            List<String> availableCards = cards.stream()
                    .map(SelenideElement::getText)
                    .collect(Collectors.toList());

            logger.error("Доступные карточки: {}", availableCards);
            throw new RuntimeException("Карточка пользователя '" + normalizedCard + "' не найдена в списке. Доступны: " + availableCards);
        }

        // Небольшая пауза для отработки клика
        Selenide.sleep(500);
    }

    /**
     * Универсальное ожидание появления блока с именем пользователя.
     */
    private void waitForUserLoaded() {
        logger.debug("Ожидание появления имени пользователя...");
        try {
            $(By.xpath(USER_NAME_BLOCK_XPATH)).shouldBe(visible, TimeoutConstants.LONG_WAIT);
        } catch (Exception e) {
            String currentUrl = WebDriverRunner.url();
            logger.error("Не удалось дождаться авторизации. Текущий URL: {}", currentUrl);
            throw new RuntimeException("Авторизация не удалась: элемент user-name не появился.", e);
        }
    }
}