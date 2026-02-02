package com.bft.ui.core.element;

import com.bft.ui.core.wait.WaitStrategy;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Умный элемент с автоматическим ожиданием и расширенной функциональностью
 */
public class SmartElement {

    private static final Logger logger = LoggerFactory.getLogger(SmartElement.class);

    private final SelenideElement element;
    private final String name;
    private final String description;
    private final WaitStrategy waitStrategy;

    public SmartElement(SelenideElement element, String name, String description, WaitStrategy waitStrategy) {
        this.element = element;
        this.name = name;
        this.description = description;
        this.waitStrategy = waitStrategy;
    }

    /**
     * Безопасный клик с автоматическим ожиданием
     */
    @Step("Кликаем по элементу: {name}")
    public SmartElement click() {
        logger.debug("Кликаем по элементу: {}", name);
        waitStrategy.waitFor(element, name);
        element.click();
        logger.info("Успешно кликнули по элементу: {}", name);
        return this;
    }

    /**
     * Двойной клик
     */
    @Step("Двойной клик по элементу: {name}")
    public SmartElement doubleClick() {
        logger.debug("Двойной клик по элементу: {}", name);
        waitStrategy.waitFor(element, name);
        new Actions(element.getWrappedDriver())
            .doubleClick(element)
            .perform();
        logger.info("Выполнен двойной клик по элементу: {}", name);
        return this;
    }

    /**
     * Наведение курсора
     */
    @Step("Наводим курсор на элемент: {name}")
    public SmartElement hover() {
        logger.debug("Наводим курсор на элемент: {}", name);
        waitStrategy.waitFor(element, name);
        element.hover();
        logger.info("Курсор наведен на элемент: {}", name);
        return this;
    }

    /**
     * Очистка поля ввода
     */
    @Step("Очищаем элемент: {name}")
    public SmartElement clear() {
        logger.debug("Очищаем элемент: {}", name);
        waitStrategy.waitFor(element, name);
        element.clear();
        logger.info("Элемент '{}' очищен", name);
        return this;
    }

    /**
     * Отправка клавиш в элемент
     */
    @Step("Отправляем клавиши '{keys}' в элемент: {name}")
    public SmartElement sendKeys(String keys) {
        logger.debug("Отправляем клавиши '{}' в элемент: {}", keys, name);
        waitStrategy.waitFor(element, name);
        element.sendKeys(keys);
        logger.info("Клавиши '{}' отправлены в элемент: {}", keys, name);
        return this;
    }

    /**
     * Ввод текста
     */
    @Step("Вводим текст '{value}' в элемент: {name}")
    public SmartElement type(String value) {
        logger.debug("Вводим текст '{}' в элемент: {}", value, name);
        waitStrategy.waitFor(element, name);
        element.clear();
        element.setValue(value);
        logger.info("Текст '{}' введен в элемент: {}", value, name);
        return this;
    }

    /**
     * Выбор из списка
     */
    @Step("Выбираем '{value}' в элементе: {name}")
    public SmartElement select(String value) {
        logger.debug("Выбираем '{}' в элементе: {}", value, name);
        waitStrategy.waitFor(element, name);
        element.selectOption(value);
        logger.info("Значение '{}' выбрано в элементе: {}", value, name);
        return this;
    }

    /**
     * Прокрутка к элементу
     */
    public SmartElement scrollIntoView() {
        logger.debug("Прокручиваем к элементу: {}", name);
        element.scrollIntoView(true);
        return this;
    }

    /**
     * Проверка видимости
     */
    public boolean isVisible() {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            logger.debug("Ошибка при проверке видимости элемента {}: {}", name, e.getMessage());
            return false;
        }
    }

    /**
     * Проверка присутствия в DOM
     */
    public boolean isPresent() {
        try {
            return element.exists();
        } catch (Exception e) {
            logger.debug("Ошибка при проверке присутствия элемента {}: {}", name, e.getMessage());
            return false;
        }
    }

    /**
     * Проверка доступности для взаимодействия
     */
    public boolean isEnabled() {
        try {
            return element.isEnabled();
        } catch (Exception e) {
            logger.debug("Ошибка при проверке доступности элемента {}: {}", name, e.getMessage());
            return false;
        }
    }

    /**
     * Получение текста
     */
    public String getText() {
        try {
            waitStrategy.waitFor(element, name);
            String text = element.getText();
            logger.debug("Получен текст '{}' из элемента: {}", text, name);
            return text;
        } catch (Exception e) {
            logger.error("Ошибка при получении текста из элемента {}: {}", name, e.getMessage());
            return "";
        }
    }

    /**
     * Получение значения атрибута
     */
    public String getAttribute(String attribute) {
        try {
            waitStrategy.waitFor(element, name);
            String value = element.getAttribute(attribute);
            logger.debug("Получен атрибут '{}' = '{}' из элемента: {}", attribute, value, name);
            return value;
        } catch (Exception e) {
            logger.error("Ошибка при получении атрибута '{}' из элемента {}: {}", attribute, name, e.getMessage());
            return "";
        }
    }

    /**
     * Проверка, что элемент содержит текст
     */
    @Step("Проверяем, что элемент '{name}' содержит текст: {expectedText}")
    public SmartElement shouldContainText(String expectedText) {
        logger.debug("Проверяем, что элемент '{}' содержит текст: {}", name, expectedText);
        waitStrategy.waitFor(element, name);
        element.shouldHave(com.codeborne.selenide.Condition.text(expectedText));
        logger.info("Элемент '{}' содержит ожидаемый текст: {}", name, expectedText);
        return this;
    }

    /**
     * Проверка, что элемент имеет CSS класс
     */
    @Step("Проверяем, что элемент '{name}' имеет класс: {cssClass}")
    public SmartElement shouldHaveClass(String cssClass) {
        logger.debug("Проверяем, что элемент '{}' имеет класс: {}", name, cssClass);
        waitStrategy.waitFor(element, name);
        element.shouldHave(com.codeborne.selenide.Condition.cssClass(cssClass));
        logger.info("Элемент '{}' имеет ожидаемый класс: {}", name, cssClass);
        return this;
    }

    /**
     * Проверка, что элемент видим
     */
    @Step("Проверяем, что элемент '{name}' видим")
    public SmartElement shouldBeVisible() {
        logger.debug("Проверяем, что элемент '{}' видим", name);
        waitStrategy.waitFor(element, name);
        element.shouldBe(com.codeborne.selenide.Condition.visible);
        logger.info("Элемент '{}' видим", name);
        return this;
    }

    /**
     * Проверка, что элемент невидим
     */
    @Step("Проверяем, что элемент '{name}' невидим")
    public SmartElement shouldBeHidden() {
        logger.debug("Проверяем, что элемент '{}' невидим", name);
        element.shouldBe(com.codeborne.selenide.Condition.hidden);
        logger.info("Элемент '{}' невидим", name);
        return this;
    }

    /**
     * Ожидание с указанной стратегией
     */
    public SmartElement waitWith(WaitStrategy strategy) {
        logger.debug("Ожидаем элемент '{}' с кастомной стратегией", name);
        strategy.waitFor(element, name);
        return this;
    }

    /**
     * Получение базового SelenideElement
     */
    public SelenideElement getElement() {
        return element;
    }

    /**
     * Получение имени элемента
     */
    public String getName() {
        return name;
    }

    /**
     * Получение описания элемента
     */
    public String getDescription() {
        return description;
    }

    /**
     * Получение стратегии ожидания
     */
    public WaitStrategy getWaitStrategy() {
        return waitStrategy;
    }

    /**
     * Загрузка файла
     */
    @Step("Загружаем файл в элемент: {name}")
    public SmartElement uploadFile(File file) {
        logger.debug("Загружаем файл '{}' в элемент: {}", file.getName(), name);
        waitStrategy.waitFor(element, name);
        element.uploadFile(file);
        logger.info("Файл '{}' загружен в элемент: {}", file.getName(), name);
        return this;
    }

    /**
     * Загрузка файла из classpath
     */
    @Step("Загружаем файл из classpath '{fileName}' в элемент: {name}")
    public SmartElement uploadFromClasspath(String fileName) {
        logger.debug("Загружаем файл '{}' из classpath в элемент: {}", fileName, name);
        waitStrategy.waitFor(element, name);
        element.uploadFromClasspath(fileName);
        logger.info("Файл '{}' из classpath загружен в элемент: {}", fileName, name);
        return this;
    }

    /**
     * Загрузка нескольких файлов из classpath
     */
    @Step("Загружаем файлы из classpath в элемент: {name}")
    public SmartElement uploadFromClasspath(String... fileNames) {
        logger.debug("Загружаем {} файлов из classpath в элемент: {}", fileNames.length, name);
        waitStrategy.waitFor(element, name);
        element.uploadFromClasspath(fileNames);
        logger.info("Файлы из classpath загружены в элемент: {}", name);
        return this;
    }

    /**
     * Выполнение JavaScript
     */
    public Object executeJavaScript(String script, Object... args) {
        logger.debug("Выполняем JavaScript в элементе: {}", name);
        return com.codeborne.selenide.Selenide.executeJavaScript(script, args);
    }

    /**
     * Получение SelenideElement (для обратной совместимости)
     */
    public SelenideElement getSelenideElement() {
        return element;
    }

    @Override
    public String toString() {
        return String.format("SmartElement{name='%s', description='%s', strategy='%s'}",
                           name, description, waitStrategy.getClass().getSimpleName());
    }
}