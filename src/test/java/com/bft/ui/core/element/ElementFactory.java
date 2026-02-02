package com.bft.ui.core.element;

import com.bft.ui.core.wait.WaitStrategy;
import com.bft.ui.core.wait.WaitStrategies;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Фабрика для создания и управления UI элементами
 * Предоставляет различные стратегии поиска и ожидания элементов
 */
public class ElementFactory {

    private static final Logger logger = LoggerFactory.getLogger(ElementFactory.class);

    // Стратегии ожидания по умолчанию
    private static final WaitStrategy DEFAULT_WAIT = WaitStrategies.visible();
    private static final WaitStrategy CLICKABLE_WAIT = WaitStrategies.clickable();
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Создание элемента по ID
     */
    public static ElementBuilder id(String id) {
        return new ElementBuilder(By.id(id), "ID: " + id);
    }

    /**
     * Создание элемента по CSS селектору
     */
    public static ElementBuilder css(String cssSelector) {
        return new ElementBuilder(cssSelector, "CSS: " + cssSelector);
    }

    /**
     * Создание элемента по XPath
     */
    public static ElementBuilder xpath(String xpath) {
        return new ElementBuilder(By.xpath(xpath), "XPath: " + xpath);
    }

    /**
     * Создание элемента по имени класса
     */
    public static ElementBuilder className(String className) {
        return new ElementBuilder(By.className(className), "Class: " + className);
    }

    /**
     * Создание элемента по имени тега
     */
    public static ElementBuilder tagName(String tagName) {
        return new ElementBuilder(By.tagName(tagName), "Tag: " + tagName);
    }

    /**
     * Создание элемента по тексту ссылки
     */
    public static ElementBuilder linkText(String linkText) {
        return new ElementBuilder(By.linkText(linkText), "Link: " + linkText);
    }

    /**
     * Создание элемента по частичному тексту ссылки
     */
    public static ElementBuilder partialLinkText(String partialLinkText) {
        return new ElementBuilder(By.partialLinkText(partialLinkText), "Partial Link: " + partialLinkText);
    }

    /**
     * Создание элемента по имени
     */
    public static ElementBuilder name(String name) {
        return new ElementBuilder(By.name(name), "Name: " + name);
    }

    /**
     * Создание элемента по произвольному By селектору
     */
    public static ElementBuilder by(By locator) {
        return new ElementBuilder(locator, "By: " + locator);
    }

    /**
     * Создание списка элементов по селектору
     */
    public static ElementListBuilder list(String cssSelector) {
        return new ElementListBuilder(cssSelector, "List CSS: " + cssSelector);
    }

    /**
     * Создание списка элементов по By селектору
     */
    public static ElementListBuilder listBy(By locator) {
        return new ElementListBuilder(locator, "List By: " + locator);
    }

    /**
     * Builder для создания одиночных элементов
     */
    public static class ElementBuilder {
        private final By locator;
        private final String description;
        private WaitStrategy waitStrategy = DEFAULT_WAIT;
        private String name = "Element";

        private ElementBuilder(By locator, String description) {
            this.locator = locator;
            this.description = description;
        }

        private ElementBuilder(String cssSelector, String description) {
            this.locator = By.cssSelector(cssSelector);
            this.description = description;
        }

        /**
         * Устанавливает имя элемента для логирования
         */
        public ElementBuilder named(String name) {
            this.name = name;
            return this;
        }

        /**
         * Устанавливает стратегию ожидания видимости
         */
        public ElementBuilder waitVisible() {
            this.waitStrategy = WaitStrategies.visible();
            return this;
        }

        /**
         * Устанавливает стратегию ожидания видимости с таймаутом
         */
        public ElementBuilder waitVisible(Duration timeout) {
            this.waitStrategy = WaitStrategies.visible(timeout);
            return this;
        }

        /**
         * Устанавливает стратегию ожидания кликабельности
         */
        public ElementBuilder waitClickable() {
            this.waitStrategy = CLICKABLE_WAIT;
            return this;
        }

        /**
         * Устанавливает стратегию ожидания кликабельности с таймаутом
         */
        public ElementBuilder waitClickable(Duration timeout) {
            this.waitStrategy = WaitStrategies.clickable(timeout);
            return this;
        }

        /**
         * Устанавливает стратегию ожидания присутствия
         */
        public ElementBuilder waitPresent() {
            this.waitStrategy = WaitStrategies.present();
            return this;
        }

        /**
         * Устанавливает кастомную стратегию ожидания
         */
        public ElementBuilder withWaitStrategy(WaitStrategy strategy) {
            this.waitStrategy = strategy;
            return this;
        }

        /**
         * Создает SmartElement с автоматическим ожиданием
         */
        public SmartElement build() {
            logger.debug("Создаем SmartElement: {} ({})", name, description);
            return new SmartElement($(locator), name, description, waitStrategy);
        }

        /**
         * Создает обычный SelenideElement без дополнительных функций
         */
        public SelenideElement buildSimple() {
            return $(locator);
        }
    }

    /**
     * Builder для создания списков элементов
     */
    public static class ElementListBuilder {
        private final By locator;
        private final String cssSelector;
        private final String description;
        private WaitStrategy waitStrategy = DEFAULT_WAIT;
        private String name = "ElementList";

        private ElementListBuilder(String cssSelector, String description) {
            this.cssSelector = cssSelector;
            this.locator = By.cssSelector(cssSelector);
            this.description = description;
        }

        private ElementListBuilder(By locator, String description) {
            this.locator = locator;
            this.cssSelector = null;
            this.description = description;
        }

        /**
         * Устанавливает имя списка для логирования
         */
        public ElementListBuilder named(String name) {
            this.name = name;
            return this;
        }

        /**
         * Устанавливает стратегию ожидания
         */
        public ElementListBuilder withWaitStrategy(WaitStrategy strategy) {
            this.waitStrategy = strategy;
            return this;
        }

        /**
         * Создает SmartElementList с автоматическим ожиданием
         */
        public SmartElementList build() {
            logger.debug("Создаем SmartElementList: {} ({})", name, description);
            return new SmartElementList($$(locator), name, description, waitStrategy);
        }

        /**
         * Создает обычный список SelenideElement
         */
        public List<SelenideElement> buildSimple() {
            return $$(locator);
        }
    }
}