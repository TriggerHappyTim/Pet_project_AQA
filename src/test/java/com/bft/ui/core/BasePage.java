package com.bft.ui.core;

import com.bft.ui.core.wait.WaitStrategy;
import com.bft.pw.By;
import com.bft.pw.Condition;
import com.bft.pw.PwDriver;
import com.bft.pw.PwWait;
import com.bft.pw.Selenide;
import com.bft.pw.SelenideElement;
import com.bft.pw.TimeoutException;
import com.bft.pw.WebDriverRunner;
import com.bft.pw.Actions;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import static com.bft.pw.Condition.cssClass;
import static com.bft.pw.Condition.enabled;
import static com.bft.pw.Condition.text;
import static com.bft.pw.Condition.value;
import static com.bft.pw.Condition.visible;
import static com.bft.pw.Selenide.$;

/**
 * Улучшенный абстрактный базовый класс для Page Objects
 * Предоставляет расширенную функциональность для работы с UI элементами
 */
public abstract class BasePage<T extends BasePage<T>> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // Конфигурация
    protected final Duration defaultTimeout = Duration.ofSeconds(10);
    protected final Duration shortTimeout = Duration.ofSeconds(3);
    protected final Duration longTimeout = Duration.ofSeconds(30);

    // Экземпляр драйвера
    protected final PwDriver driver = WebDriverRunner.getWebDriver();

    /**
     * Получение URL страницы
     */
    protected abstract String getPageUrl();

    /**
     * Получение заголовка страницы
     */
    protected abstract String getPageTitle();

    /**
     * Проверка, что страница загружена корректно
     */
    protected abstract boolean isPageLoaded();

    /**
     * Метод для fluent API - возвращает this с правильным типом
     */
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    /**
     * Переход на страницу
     */
    @Step("Открываем страницу: {pageName}")
    public T openPage(String pageName) {
        logger.info("Открываем страницу: {}", pageName);
        Selenide.open(getPageUrl());

        // Ждем загрузки страницы
        waitForPageLoad();

        // Делаем скриншот при открытии
        takeScreenshotOnOpen(pageName);

        return self();
    }

    /**
     * Переход на страницу с проверкой
     */
    @Step("Открываем и проверяем страницу: {pageName}")
    public T openPageAndVerify(String pageName) {
        openPage(pageName);

        if (!isPageLoaded()) {
            logger.error("Страница {} не загружена корректно", pageName);
            takeScreenshotOnError("page_load_failed_" + pageName);
            throw new PageLoadException("Страница " + pageName + " не загружена корректно");
        }

        logger.info("Страница {} успешно открыта и проверена", pageName);
        return self();
    }

    /**
     * Ожидание загрузки страницы
     */
    @Step("Ожидаем загрузки страницы")
    public T waitForPageLoad() {
        logger.debug("Ожидаем загрузки страницы");

        // Ожидаем, пока документ будет полностью загружен
        waitForCondition(driver -> {
            try {
                return "complete".equals(driver.executeScript("return document.readyState"));
            } catch (Exception e) {
                return false;
            }
        }, longTimeout, "Страница не загрузилась");

        // Дополнительное ожидание для динамического контента - проверяем отсутствие спиннеров загрузки
        try {
            $(By.xpath("//div[contains(@class, 'spinner') or contains(@class, 'loading')]"))
                .should(Condition.disappear, shortTimeout);
        } catch (Exception e) {
            // Если спиннеры не найдены или уже исчезли, продолжаем выполнение
            logger.debug("Спиннеры загрузки не найдены или уже исчезли");
        }

        return self();
    }

    /**
     * Ожидание с кастомной стратегией
     */
    public T waitWithStrategy(WaitStrategy strategy, String description) {
        logger.debug("Ожидаем с стратегией: {}", description);
        strategy.wait(description);
        return self();
    }

    /**
     * Ожидание элемента с использованием стратегии
     */
    public T waitForElement(SelenideElement element, WaitStrategy strategy, String elementName) {
        logger.debug("Ожидаем элемент '{}' с стратегией", elementName);
        strategy.waitFor(element, elementName);
        return self();
    }

    /**
     * Ожидание условия
     */
    public T waitForCondition(Function<PwDriver, Boolean> condition, Duration timeout, String errorMessage) {
        try {
            new PwWait(timeout, Duration.ofMillis(100))
                .until(condition);
        } catch (TimeoutException e) {
            logger.error("Таймаут при ожидании условия: {}", errorMessage);
            takeScreenshotOnError("timeout_" + System.currentTimeMillis());
            throw new TimeoutException(errorMessage, e);
        }
        return self();
    }

    /**
     * Безопасный клик по элементу
     */
    @Step("Кликаем по элементу: {elementName}")
    public T clickElement(SelenideElement element, String elementName) {
        logger.debug("Кликаем по элементу: {}", elementName);

        try {
            // Ждем элемент
            element.shouldBe(visible, defaultTimeout);

            // Прокручиваем к элементу
            scrollToElement(element, elementName);

            // Кликаем
            element.click();

            logger.info("Успешно кликнули по элементу: {}", elementName);

        } catch (Exception e) {
            logger.error("Ошибка при клике по элементу '{}': {}", elementName, e.getMessage());
            takeScreenshotOnError("click_failed_" + elementName);
            throw new ElementInteractionException("Не удалось кликнуть по элементу: " + elementName, e);
        }

        return self();
    }

    /**
     * Безопасный ввод текста
     */
    @Step("Вводим текст '{value}' в поле: {fieldName}")
    public T typeText(SelenideElement element, String value, String fieldName) {
        logger.debug("Вводим текст '{}' в поле: {}", value, fieldName);

        try {
            // Ждем элемент
            element.shouldBe(visible, defaultTimeout);

            // Очищаем и вводим текст
            element.clear();
            element.setValue(value);

            // Проверяем, что текст введен
            element.shouldHave(value(value));

            logger.info("Текст '{}' успешно введен в поле: {}", value, fieldName);

        } catch (Exception e) {
            logger.error("Ошибка при вводе текста в поле '{}': {}", fieldName, e.getMessage());
            takeScreenshotOnError("type_failed_" + fieldName);
            throw new ElementInteractionException("Не удалось ввести текст в поле: " + fieldName, e);
        }

        return self();
    }

    /**
     * Выбор из выпадающего списка
     */
    @Step("Выбираем значение '{value}' в списке: {selectName}")
    public T selectFromDropdown(SelenideElement selectElement, String value, String selectName) {
        logger.debug("Выбираем '{}' в списке: {}", value, selectName);

        try {
            selectElement.shouldBe(visible, defaultTimeout);
            selectElement.selectOption(value);

            logger.info("Значение '{}' выбрано в списке: {}", value, selectName);

        } catch (Exception e) {
            logger.error("Ошибка при выборе в списке '{}': {}", selectName, e.getMessage());
            takeScreenshotOnError("select_failed_" + selectName);
            throw new ElementInteractionException("Не удалось выбрать значение в списке: " + selectName, e);
        }

        return self();
    }

    /**
     * Проверка, что элемент содержит текст
     */
    @Step("Проверяем, что элемент '{elementName}' содержит текст: {expectedText}")
    public T verifyElementText(SelenideElement element, String expectedText, String elementName) {
        logger.debug("Проверяем текст '{}' в элементе: {}", expectedText, elementName);

        try {
            element.shouldBe(visible, defaultTimeout)
                   .shouldHave(text(expectedText));

            logger.info("Текст '{}' найден в элементе: {}", expectedText, elementName);

        } catch (Exception e) {
            logger.error("Текст '{}' не найден в элементе '{}': {}", expectedText, elementName, e.getMessage());
            takeScreenshotOnError("text_verify_failed_" + elementName);
            throw new AssertionError("Текст не найден в элементе: " + elementName, e);
        }

        return self();
    }

    /**
     * Проверка, что элемент имеет определенный класс
     */
    @Step("Проверяем, что элемент '{elementName}' имеет класс: {cssClass}")
    public T verifyElementHasClass(SelenideElement element, String cssClass, String elementName) {
        logger.debug("Проверяем класс '{}' у элемента: {}", cssClass, elementName);

        try {
            element.shouldBe(visible, defaultTimeout)
                   .shouldHave(cssClass(cssClass));

            logger.info("Класс '{}' найден у элемента: {}", cssClass, elementName);

        } catch (Exception e) {
            logger.error("Класс '{}' не найден у элемента '{}': {}", cssClass, elementName, e.getMessage());
            takeScreenshotOnError("class_verify_failed_" + elementName);
            throw new AssertionError("Класс не найден у элемента: " + elementName, e);
        }

        return self();
    }

    /**
     * Прокрутка к элементу
     */
    public T scrollToElement(SelenideElement element, String elementName) {
        logger.debug("Прокручиваем к элементу: {}", elementName);

        try {
            element.scrollIntoView(true);
            // Ожидаем, что элемент стал видимым после прокрутки
            element.shouldBe(visible, shortTimeout);

        } catch (Exception e) {
            logger.warn("Не удалось прокрутить к элементу '{}': {}", elementName, e.getMessage());
        }

        return self();
    }

    /**
     * Наведение курсора на элемент
     */
    @Step("Наводим курсор на элемент: {elementName}")
    public T hoverElement(SelenideElement element, String elementName) {
        logger.debug("Наводим курсор на элемент: {}", elementName);

        try {
            new Actions(driver)
                .moveToElement(element)
                .perform();

            logger.info("Курсор наведен на элемент: {}", elementName);

        } catch (Exception e) {
            logger.error("Ошибка при наведении курсора на элемент '{}': {}", elementName, e.getMessage());
            takeScreenshotOnError("hover_failed_" + elementName);
            throw new ElementInteractionException("Не удалось навести курсор на элемент: " + elementName, e);
        }

        return self();
    }

    /**
     * Двойной клик по элементу
     */
    @Step("Двойной клик по элементу: {elementName}")
    public T doubleClickElement(SelenideElement element, String elementName) {
        logger.debug("Двойной клик по элементу: {}", elementName);

        try {
            new Actions(driver)
                .doubleClick(element)
                .perform();

            logger.info("Двойной клик выполнен по элементу: {}", elementName);

        } catch (Exception e) {
            logger.error("Ошибка при двойном клике по элементу '{}': {}", elementName, e.getMessage());
            takeScreenshotOnError("double_click_failed_" + elementName);
            throw new ElementInteractionException("Не удалось выполнить двойной клик по элементу: " + elementName, e);
        }

        return self();
    }

    /**
     * Перетаскивание элемента
     */
    @Step("Перетаскиваем элемент '{sourceName}' на элемент '{targetName}'")
    public T dragAndDrop(SelenideElement source, String sourceName, SelenideElement target, String targetName) {
        logger.debug("Перетаскиваем '{}' на '{}'", sourceName, targetName);

        try {
            new Actions(driver)
                .dragAndDrop(source, target)
                .perform();

            logger.info("Элемент '{}' успешно перетащен на '{}'", sourceName, targetName);

        } catch (Exception e) {
            logger.error("Ошибка при перетаскивании '{}' на '{}': {}", sourceName, targetName, e.getMessage());
            takeScreenshotOnError("drag_drop_failed_" + sourceName + "_to_" + targetName);
            throw new ElementInteractionException("Не удалось перетащить элемент", e);
        }

        return self();
    }

    /**
     * Получение текста элемента
     */
    public String getElementText(SelenideElement element, String elementName) {
        try {
            element.shouldBe(visible, defaultTimeout);
            String text = element.getText();
            logger.debug("Получен текст '{}' из элемента: {}", text, elementName);
            return text;
        } catch (Exception e) {
            logger.error("Не удалось получить текст из элемента '{}': {}", elementName, e.getMessage());
            return "";
        }
    }

    /**
     * Получение значения атрибута элемента
     */
    public String getElementAttribute(SelenideElement element, String attribute, String elementName) {
        try {
            element.shouldBe(visible, defaultTimeout);
            String value = element.getAttribute(attribute);
            logger.debug("Получен атрибут '{}' = '{}' из элемента: {}", attribute, value, elementName);
            return value;
        } catch (Exception e) {
            logger.error("Не удалось получить атрибут '{}' из элемента '{}': {}", attribute, elementName, e.getMessage());
            return "";
        }
    }

    /**
     * Проверка, что элемент существует
     */
    public boolean isElementPresent(SelenideElement element, String elementName) {
        try {
            boolean present = element.exists();
            logger.debug("Элемент '{}' {}", elementName, present ? "присутствует" : "отсутствует");
            return present;
        } catch (Exception e) {
            logger.debug("Ошибка при проверке присутствия элемента '{}': {}", elementName, e.getMessage());
            return false;
        }
    }

    /**
     * Проверка, что элемент видим
     */
    public boolean isElementVisible(SelenideElement element, String elementName) {
        try {
            boolean isVisible = element.is(visible);
            logger.debug("Элемент '{}' {}", elementName, isVisible ? "видим" : "невидим");
            return isVisible;
        } catch (Exception e) {
            logger.debug("Ошибка при проверке видимости элемента '{}': {}", elementName, e.getMessage());
            return false;
        }
    }

    /**
     * Проверка, что элемент доступен для взаимодействия
     */
    public boolean isElementEnabled(SelenideElement element, String elementName) {
        try {
            boolean isEnabled = element.is(enabled);
            logger.debug("Элемент '{}' {}", elementName, isEnabled ? "доступен" : "недоступен");
            return isEnabled;
        } catch (Exception e) {
            logger.debug("Ошибка при проверке доступности элемента '{}': {}", elementName, e.getMessage());
            return false;
        }
    }

    /**
     * Получение списка элементов
     */
    public List<SelenideElement> getElementsList(SelenideElement container, String containerName) {
        try {
            List<SelenideElement> elements = container.$$("*").filter(visible);
            logger.debug("Найдено {} элементов в контейнере: {}", elements.size(), containerName);
            return elements;
        } catch (Exception e) {
            logger.error("Ошибка при получении списка элементов из контейнера '{}': {}", containerName, e.getMessage());
            return List.of();
        }
    }

    /**
     * Сделать скриншот страницы
     */
    @Step("Делаем скриншот страницы: {name}")
    public T takeScreenshot(String name) {
        try {
            String filename = name + "_" + System.currentTimeMillis();
            Selenide.screenshot(filename);
            logger.info("Скриншот сохранен: {}", filename);
        } catch (Exception e) {
            logger.warn("Не удалось сделать скриншот '{}': {}", name, e.getMessage());
        }
        return self();
    }

    /**
     * Сделать скриншот при открытии страницы
     */
    private void takeScreenshotOnOpen(String pageName) {
        if (shouldTakeScreenshotOnOpen()) {
            takeScreenshot("page_open_" + pageName);
        }
    }

    /**
     * Сделать скриншот при ошибке
     */
    private void takeScreenshotOnError(String errorContext) {
        if (shouldTakeScreenshotOnError()) {
            takeScreenshot("error_" + errorContext);
        }
    }

    /**
     * Определяет, нужно ли делать скриншот при открытии страницы
     */
    protected boolean shouldTakeScreenshotOnOpen() {
        return Boolean.parseBoolean(System.getProperty("pageobject.screenshot.onOpen", "false"));
    }

    /**
     * Определяет, нужно ли делать скриншот при ошибке
     */
    protected boolean shouldTakeScreenshotOnError() {
        return Boolean.parseBoolean(System.getProperty("pageobject.screenshot.onError", "true"));
    }

    /**
     * Получение конфигурируемого таймаута
     */
    protected Duration getConfiguredTimeout(String timeoutType) {
        String timeoutProp = System.getProperty("pageobject.timeout." + timeoutType);
        if (timeoutProp != null) {
            try {
                return Duration.ofSeconds(Long.parseLong(timeoutProp));
            } catch (NumberFormatException e) {
                logger.warn("Неверный формат таймаута '{}', используем значение по умолчанию", timeoutProp);
            }
        }

        // Значения по умолчанию
        switch (timeoutType) {
            case "short": return shortTimeout;
            case "long": return longTimeout;
            default: return defaultTimeout;
        }
    }

    /**
     * Выполнение JavaScript
     */
    protected Object executeJavaScript(String script, Object... args) {
        try {
            return Selenide.executeJavaScript(script, args);
        } catch (Exception e) {
            logger.error("Ошибка при выполнении JavaScript: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Ожидание выполнения JavaScript условия
     */
    public T waitForJsCondition(String condition, Duration timeout, String description) {
        waitForCondition(driver -> {
            try {
                Object result = driver.executeScript("return " + condition);
                return Boolean.TRUE.equals(result);
            } catch (Exception e) {
                return false;
            }
        }, timeout, "JavaScript условие не выполнено: " + description);

        return self();
    }

    // Вспомогательные классы исключений

    public static class PageLoadException extends RuntimeException {
        public PageLoadException(String message) {
            super(message);
        }

        public PageLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ElementInteractionException extends RuntimeException {
        public ElementInteractionException(String message) {
            super(message);
        }

        public ElementInteractionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}