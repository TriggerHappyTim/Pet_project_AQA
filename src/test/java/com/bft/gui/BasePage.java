package com.bft.gui;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.url;

/**
 * Базовый класс для всех Page Object.
 * Реализует шаблонный метод для проверки загрузки страницы и предоставляет утилиты Selenide.
 *
 * @param <T> тип самого класса страницы (для Fluent Interface)
 */
public abstract class BasePage<T extends BasePage<T>> {

    private static final Logger logger = LoggerFactory.getLogger(BasePage.class);

    /**
     * Возвращает относительный URL страницы (путь после домена).
     * Должен быть реализован в наследниках.
     */
    protected abstract String getPageUrl();

    /**
     * Возвращает ожидаемый заголовок страницы (или его часть).
     * Может возвращать null, если проверка заголовка не требуется.
     */
    protected String getPageTitle() {
        return null;
    }

    /**
     * Определяет условие загрузки страницы.
     * По умолчанию проверяет наличие уникального элемента по селектору.
     * Переопределите для сложной логики.
     */
    protected boolean isPageLoaded() {
        try {
            // Попытка найти уникальный элемент, если он задан в наследнике через getUniqueElementSelector()
            String selector = getUniqueElementSelector();
            if (selector != null && !selector.isEmpty()) {
                return $(selector).is(visible);
            }
            // Fallback: проверка URL
            return url().contains(getPageUrl());
        } catch (Exception e) {
            logger.debug("Страница еще не загружена: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Возвращает CSS или XPath селектор уникального элемента страницы для проверки загрузки.
     * Переопределите в наследнике для точной проверки.
     */
    protected String getUniqueElementSelector() {
        return null;
    }

    /**
     * Открывает страницу и ожидает её загрузки.
     */
    @Step("Открытие страницы: {pageName}")
    public T openPage(String pageName) {
        logger.info("Открытие страницы: {}", pageName);
        String fullUrl = getFullUrl();

        // Проверка, чтобы не открывать одну и ту же страницу дважды
        if (!url().equals(fullUrl)) {
            open(fullUrl);
        }

        waitForPageLoad();
        return self();
    }

    /**
     * Открывает страницу, проверяет URL и заголовок.
     */
    @Step("Открытие и верификация страницы: {pageName}")
    public T openPageAndVerify(String pageName) {
        openPage(pageName);
        verifyPage();
        return self();
    }

    /**
     * Проверяет, открыта ли текущая страница (сравнение URL).
     */
    public boolean isPageOpened(String expectedUrlPart) {
        try {
            return url().contains(expectedUrlPart);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ожидает загрузки страницы с таймаутом.
     */
    @Step("Ожидание загрузки страницы")
    public T waitForPageLoad() {
        logger.debug("Ожидание загрузки страницы...");
        try {
            // Используем встроенный механизм ожидания Selenide через should
            // Ждем, пока предикат isPageLoaded() станет true
            SelenideElement dummy = $("body"); // Любой существующий элемент для привязки условия

            // Простая реализация ожидания через цикл, так как waitUntil может отсутствовать в старых версиях
            long timeout = 10000; // 10 секунд
            long interval = 500;
            long start = System.currentTimeMillis();

            while (!isPageLoaded()) {
                if (System.currentTimeMillis() - start > timeout) {
                    throw new AssertionError("Страница не загрузилась за " + timeout + " мс. URL: " + url());
                }
                Thread.sleep(interval);
            }

            logger.info("Страница успешно загружена");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ожидание прервано", e);
        }
        return self();
    }

    /**
     * Проверяет URL и заголовок страницы.
     */
    @Step("Верификация страницы")
    public void verifyPage() {
        String currentUrl = url();
        String expectedUrlPart = getPageUrl();

        if (!currentUrl.contains(expectedUrlPart)) {
            throw new AssertionError(String.format(
                    "Неверный URL. Ожидалось содержащее '%s', но получено '%s'",
                    expectedUrlPart, currentUrl
            ));
        }

        String expectedTitle = getPageTitle();
        if (expectedTitle != null && !title().contains(expectedTitle)) {
            throw new AssertionError(String.format(
                    "Неверный заголовок. Ожидалось содержащее '%s', но получено '%s'",
                    expectedTitle, title()
            ));
        }

        logger.info("Страница верифицирована: URL={}, Title={}", currentUrl, title());
    }

    /**
     * Возвращает полный URL (базовый + путь).
     * Если базовый URL не задан в конфиге, берется относительный путь (Selenide сам подставит домен).
     */
    protected String getFullUrl() {
        String path = getPageUrl();
        if (path.startsWith("http")) {
            return path;
        }
        // Для относительных путей Selenide open() сам склеит с baseUrl, если он задан в Configuration.baseUrl
        // Но для явной проверки лучше вернуть просто путь
        return path;
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }
}