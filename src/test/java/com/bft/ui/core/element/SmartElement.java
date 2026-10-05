package com.bft.ui.core.element;

import com.bft.ui.core.ClickHelper;
import com.bft.ui.core.wait.WaitStrategy;
import com.bft.pw.Keys;
import com.bft.pw.Actions;
import com.bft.pw.SelenideElement;
import com.bft.pw.WebDriverRunner;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * РЈРјРЅС‹Р№ СЌР»РµРјРµРЅС‚ СЃ Р°РІС‚РѕРјР°С‚РёС‡РµСЃРєРёРј РѕР¶РёРґР°РЅРёРµРј Рё СЂР°СЃС€РёСЂРµРЅРЅРѕР№ С„СѓРЅРєС†РёРѕРЅР°Р»СЊРЅРѕСЃС‚СЊСЋ
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
     * Р‘РµР·РѕРїР°СЃРЅС‹Р№ РєР»РёРє СЃ Р°РІС‚РѕРјР°С‚РёС‡РµСЃРєРёРј РѕР¶РёРґР°РЅРёРµРј.
     * РџСЂРё РїРµСЂРµС…РІР°С‚Рµ РєР»РёРєР° РґСЂСѓРіРёРј СЌР»РµРјРµРЅС‚РѕРј РІС‹РїРѕР»РЅСЏРµС‚СЃСЏ JS-РєР»РёРє (СЃРј. {@link ClickHelper}).
     */
    @Step("РљР»РёРєР°РµРј РїРѕ СЌР»РµРјРµРЅС‚Сѓ: {name}")
public SmartElement click() {
        logger.debug("Кликаем по элементу: {}", name);
        try {
            waitStrategy.waitFor(element, name);
            ClickHelper.click(element, name);
        } catch (Throwable t) {
            WebDriverRunner.saveSourceDump("SmartElement-click-" + name.replaceAll("[^\\w\\-а-яА-ЯёЁ ]", "_"));
            throw t;
        }
        logger.info("Успешно кликнули по элементу: {}", name);
        return this;
    }

    /**
     * Р”РІРѕР№РЅРѕР№ РєР»РёРє
     */
    @Step("Р”РІРѕР№РЅРѕР№ РєР»РёРє РїРѕ СЌР»РµРјРµРЅС‚Сѓ: {name}")
    public SmartElement doubleClick() {
        logger.debug("Р”РІРѕР№РЅРѕР№ РєР»РёРє РїРѕ СЌР»РµРјРµРЅС‚Сѓ: {}", name);
        waitStrategy.waitFor(element, name);
        new Actions(element.getWrappedDriver())
            .doubleClick(element)
            .perform();
        logger.info("Р’С‹РїРѕР»РЅРµРЅ РґРІРѕР№РЅРѕР№ РєР»РёРє РїРѕ СЌР»РµРјРµРЅС‚Сѓ: {}", name);
        return this;
    }

    /**
     * РќР°РІРµРґРµРЅРёРµ РєСѓСЂСЃРѕСЂР°
     */
    @Step("РќР°РІРѕРґРёРј РєСѓСЂСЃРѕСЂ РЅР° СЌР»РµРјРµРЅС‚: {name}")
    public SmartElement hover() {
        logger.debug("РќР°РІРѕРґРёРј РєСѓСЂСЃРѕСЂ РЅР° СЌР»РµРјРµРЅС‚: {}", name);
        waitStrategy.waitFor(element, name);
        element.hover();
        logger.info("РљСѓСЂСЃРѕСЂ РЅР°РІРµРґРµРЅ РЅР° СЌР»РµРјРµРЅС‚: {}", name);
        return this;
    }

    /**
     * РћС‡РёСЃС‚РєР° РїРѕР»СЏ РІРІРѕРґР°
     */
    @Step("РћС‡РёС‰Р°РµРј СЌР»РµРјРµРЅС‚: {name}")
    public SmartElement clear() {
        logger.debug("РћС‡РёС‰Р°РµРј СЌР»РµРјРµРЅС‚: {}", name);
        waitStrategy.waitFor(element, name);
        element.clear();
        logger.info("Р­Р»РµРјРµРЅС‚ '{}' РѕС‡РёС‰РµРЅ", name);
        return this;
    }

    /**
     * РћС‚РїСЂР°РІРєР° РєР»Р°РІРёС€ РІ СЌР»РµРјРµРЅС‚
     */
    @Step("РћС‚РїСЂР°РІР»СЏРµРј РєР»Р°РІРёС€Рё '{keys}' РІ СЌР»РµРјРµРЅС‚: {name}")
    public SmartElement sendKeys(String keys) {
        logger.debug("РћС‚РїСЂР°РІР»СЏРµРј РєР»Р°РІРёС€Рё '{}' РІ СЌР»РµРјРµРЅС‚: {}", keys, name);
        waitStrategy.waitFor(element, name);
        element.sendKeys(keys);
        logger.info("РљР»Р°РІРёС€Рё '{}' РѕС‚РїСЂР°РІР»РµРЅС‹ РІ СЌР»РµРјРµРЅС‚: {}", keys, name);
        return this;
    }

    /**
     * Р’РІРѕРґ С‚РµРєСЃС‚Р°.
     * Р•СЃР»Рё clear() РїР°РґР°РµС‚ (MUI/React controlled input), РІРІРѕРґ С‡РµСЂРµР· Ctrl+A Рё sendKeys.
     */
    @Step("Р’РІРѕРґРёРј С‚РµРєСЃС‚ '{value}' РІ СЌР»РµРјРµРЅС‚: {name}")
    public SmartElement type(String value) {
        logger.debug("Р’РІРѕРґРёРј С‚РµРєСЃС‚ '{}' РІ СЌР»РµРјРµРЅС‚: {}", value, name);
        waitStrategy.waitFor(element, name);
        try {
            element.clear();
            element.setValue(value);
        } catch (Throwable e) {
            logger.debug("clear/setValue РїСЂРѕРїСѓС‰РµРЅ РґР»СЏ СЌР»РµРјРµРЅС‚Р° {}, РІРІРѕРґ С‡РµСЂРµР· Ctrl+A+sendKeys: {}", name, e.getMessage());
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            element.sendKeys(value);
        }
        logger.info("РўРµРєСЃС‚ '{}' РІРІРµРґРµРЅ РІ СЌР»РµРјРµРЅС‚: {}", value, name);
        return this;
    }

    /**
     * Р’С‹Р±РѕСЂ РёР· СЃРїРёСЃРєР°
     */
    @Step("Р’С‹Р±РёСЂР°РµРј '{value}' РІ СЌР»РµРјРµРЅС‚Рµ: {name}")
    public SmartElement select(String value) {
        logger.debug("Р’С‹Р±РёСЂР°РµРј '{}' РІ СЌР»РµРјРµРЅС‚Рµ: {}", value, name);
        waitStrategy.waitFor(element, name);
        element.selectOption(value);
        logger.info("Р—РЅР°С‡РµРЅРёРµ '{}' РІС‹Р±СЂР°РЅРѕ РІ СЌР»РµРјРµРЅС‚Рµ: {}", value, name);
        return this;
    }

    /**
     * РџСЂРѕРєСЂСѓС‚РєР° Рє СЌР»РµРјРµРЅС‚Сѓ
     */
    public SmartElement scrollIntoView() {
        logger.debug("РџСЂРѕРєСЂСѓС‡РёРІР°РµРј Рє СЌР»РµРјРµРЅС‚Сѓ: {}", name);
        element.scrollIntoView(true);
        return this;
    }

    /**
     * РџСЂРѕРІРµСЂРєР° РІРёРґРёРјРѕСЃС‚Рё
     */
    public boolean isVisible() {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            logger.debug("РћС€РёР±РєР° РїСЂРё РїСЂРѕРІРµСЂРєРµ РІРёРґРёРјРѕСЃС‚Рё СЌР»РµРјРµРЅС‚Р° {}: {}", name, e.getMessage());
            return false;
        }
    }

    /**
     * РџСЂРѕРІРµСЂРєР° РїСЂРёСЃСѓС‚СЃС‚РІРёСЏ РІ DOM
     */
    public boolean isPresent() {
        try {
            return element.exists();
        } catch (Exception e) {
            logger.debug("РћС€РёР±РєР° РїСЂРё РїСЂРѕРІРµСЂРєРµ РїСЂРёСЃСѓС‚СЃС‚РІРёСЏ СЌР»РµРјРµРЅС‚Р° {}: {}", name, e.getMessage());
            return false;
        }
    }

    /**
     * РџСЂРѕРІРµСЂРєР° РґРѕСЃС‚СѓРїРЅРѕСЃС‚Рё РґР»СЏ РІР·Р°РёРјРѕРґРµР№СЃС‚РІРёСЏ
     */
    public boolean isEnabled() {
        try {
            return element.isEnabled();
        } catch (Exception e) {
            logger.debug("РћС€РёР±РєР° РїСЂРё РїСЂРѕРІРµСЂРєРµ РґРѕСЃС‚СѓРїРЅРѕСЃС‚Рё СЌР»РµРјРµРЅС‚Р° {}: {}", name, e.getMessage());
            return false;
        }
    }

    /**
     * РџРѕР»СѓС‡РµРЅРёРµ С‚РµРєСЃС‚Р°
     */
    public String getText() {
        try {
            waitStrategy.waitFor(element, name);
            String text = element.getText();
            logger.debug("РџРѕР»СѓС‡РµРЅ С‚РµРєСЃС‚ '{}' РёР· СЌР»РµРјРµРЅС‚Р°: {}", text, name);
            return text;
        } catch (Exception e) {
            logger.error("РћС€РёР±РєР° РїСЂРё РїРѕР»СѓС‡РµРЅРёРё С‚РµРєСЃС‚Р° РёР· СЌР»РµРјРµРЅС‚Р° {}: {}", name, e.getMessage());
            return "";
        }
    }

    /**
     * РџРѕР»СѓС‡РµРЅРёРµ Р·РЅР°С‡РµРЅРёСЏ Р°С‚СЂРёР±СѓС‚Р°
     */
    public String getAttribute(String attribute) {
        try {
            waitStrategy.waitFor(element, name);
            String value = element.getAttribute(attribute);
            logger.debug("РџРѕР»СѓС‡РµРЅ Р°С‚СЂРёР±СѓС‚ '{}' = '{}' РёР· СЌР»РµРјРµРЅС‚Р°: {}", attribute, value, name);
            return value;
        } catch (Exception e) {
            logger.error("РћС€РёР±РєР° РїСЂРё РїРѕР»СѓС‡РµРЅРёРё Р°С‚СЂРёР±СѓС‚Р° '{}' РёР· СЌР»РµРјРµРЅС‚Р° {}: {}", attribute, name, e.getMessage());
            return "";
        }
    }

    /**
     * РџСЂРѕРІРµСЂРєР°, С‡С‚Рѕ СЌР»РµРјРµРЅС‚ СЃРѕРґРµСЂР¶РёС‚ С‚РµРєСЃС‚
     */
    @Step("РџСЂРѕРІРµСЂСЏРµРј, С‡С‚Рѕ СЌР»РµРјРµРЅС‚ '{name}' СЃРѕРґРµСЂР¶РёС‚ С‚РµРєСЃС‚: {expectedText}")
    public SmartElement shouldContainText(String expectedText) {
        logger.debug("РџСЂРѕРІРµСЂСЏРµРј, С‡С‚Рѕ СЌР»РµРјРµРЅС‚ '{}' СЃРѕРґРµСЂР¶РёС‚ С‚РµРєСЃС‚: {}", name, expectedText);
        waitStrategy.waitFor(element, name);
        element.shouldHave(com.bft.pw.Condition.text(expectedText));
        logger.info("Р­Р»РµРјРµРЅС‚ '{}' СЃРѕРґРµСЂР¶РёС‚ РѕР¶РёРґР°РµРјС‹Р№ С‚РµРєСЃС‚: {}", name, expectedText);
        return this;
    }

    /**
     * РџСЂРѕРІРµСЂРєР°, С‡С‚Рѕ СЌР»РµРјРµРЅС‚ РёРјРµРµС‚ CSS РєР»Р°СЃСЃ
     */
    @Step("РџСЂРѕРІРµСЂСЏРµРј, С‡С‚Рѕ СЌР»РµРјРµРЅС‚ '{name}' РёРјРµРµС‚ РєР»Р°СЃСЃ: {cssClass}")
    public SmartElement shouldHaveClass(String cssClass) {
        logger.debug("РџСЂРѕРІРµСЂСЏРµРј, С‡С‚Рѕ СЌР»РµРјРµРЅС‚ '{}' РёРјРµРµС‚ РєР»Р°СЃСЃ: {}", name, cssClass);
        waitStrategy.waitFor(element, name);
        element.shouldHave(com.bft.pw.Condition.cssClass(cssClass));
        logger.info("Р­Р»РµРјРµРЅС‚ '{}' РёРјРµРµС‚ РѕР¶РёРґР°РµРјС‹Р№ РєР»Р°СЃСЃ: {}", name, cssClass);
        return this;
    }

    /**
     * РџСЂРѕРІРµСЂРєР°, С‡С‚Рѕ СЌР»РµРјРµРЅС‚ РІРёРґРёРј
     */
    @Step("РџСЂРѕРІРµСЂСЏРµРј, С‡С‚Рѕ СЌР»РµРјРµРЅС‚ '{name}' РІРёРґРёРј")
    public SmartElement shouldBeVisible() {
        logger.debug("РџСЂРѕРІРµСЂСЏРµРј, С‡С‚Рѕ СЌР»РµРјРµРЅС‚ '{}' РІРёРґРёРј", name);
        waitStrategy.waitFor(element, name);
        element.shouldBe(com.bft.pw.Condition.visible);
        logger.info("Р­Р»РµРјРµРЅС‚ '{}' РІРёРґРёРј", name);
        return this;
    }

    /**
     * РџСЂРѕРІРµСЂРєР°, С‡С‚Рѕ СЌР»РµРјРµРЅС‚ РЅРµРІРёРґРёРј
     */
    @Step("РџСЂРѕРІРµСЂСЏРµРј, С‡С‚Рѕ СЌР»РµРјРµРЅС‚ '{name}' РЅРµРІРёРґРёРј")
    public SmartElement shouldBeHidden() {
        logger.debug("РџСЂРѕРІРµСЂСЏРµРј, С‡С‚Рѕ СЌР»РµРјРµРЅС‚ '{}' РЅРµРІРёРґРёРј", name);
        element.shouldBe(com.bft.pw.Condition.hidden);
        logger.info("Р­Р»РµРјРµРЅС‚ '{}' РЅРµРІРёРґРёРј", name);
        return this;
    }

    /**
     * РћР¶РёРґР°РЅРёРµ СЃ СѓРєР°Р·Р°РЅРЅРѕР№ СЃС‚СЂР°С‚РµРіРёРµР№
     */
    public SmartElement waitWith(WaitStrategy strategy) {
        logger.debug("РћР¶РёРґР°РµРј СЌР»РµРјРµРЅС‚ '{}' СЃ РєР°СЃС‚РѕРјРЅРѕР№ СЃС‚СЂР°С‚РµРіРёРµР№", name);
        strategy.waitFor(element, name);
        return this;
    }

    /**
     * РџРѕР»СѓС‡РµРЅРёРµ Р±Р°Р·РѕРІРѕРіРѕ SelenideElement
     */
    public SelenideElement getElement() {
        return element;
    }

    /**
     * РџРѕР»СѓС‡РµРЅРёРµ РёРјРµРЅРё СЌР»РµРјРµРЅС‚Р°
     */
    public String getName() {
        return name;
    }

    /**
     * РџРѕР»СѓС‡РµРЅРёРµ РѕРїРёСЃР°РЅРёСЏ СЌР»РµРјРµРЅС‚Р°
     */
    public String getDescription() {
        return description;
    }

    /**
     * РџРѕР»СѓС‡РµРЅРёРµ СЃС‚СЂР°С‚РµРіРёРё РѕР¶РёРґР°РЅРёСЏ
     */
    public WaitStrategy getWaitStrategy() {
        return waitStrategy;
    }

    /**
     * Р—Р°РіСЂСѓР·РєР° С„Р°Р№Р»Р°
     */
    @Step("Р—Р°РіСЂСѓР¶Р°РµРј С„Р°Р№Р» РІ СЌР»РµРјРµРЅС‚: {name}")
    public SmartElement uploadFile(File file) {
        logger.debug("Р—Р°РіСЂСѓР¶Р°РµРј С„Р°Р№Р» '{}' РІ СЌР»РµРјРµРЅС‚: {}", file.getName(), name);
        waitStrategy.waitFor(element, name);
        element.uploadFile(file);
        logger.info("Р¤Р°Р№Р» '{}' Р·Р°РіСЂСѓР¶РµРЅ РІ СЌР»РµРјРµРЅС‚: {}", file.getName(), name);
        return this;
    }

    /**
     * Р—Р°РіСЂСѓР·РєР° С„Р°Р№Р»Р° РёР· classpath
     */
    @Step("Р—Р°РіСЂСѓР¶Р°РµРј С„Р°Р№Р» РёР· classpath '{fileName}' РІ СЌР»РµРјРµРЅС‚: {name}")
    public SmartElement uploadFromClasspath(String fileName) {
        logger.debug("Р—Р°РіСЂСѓР¶Р°РµРј С„Р°Р№Р» '{}' РёР· classpath РІ СЌР»РµРјРµРЅС‚: {}", fileName, name);
        waitStrategy.waitFor(element, name);
        element.uploadFromClasspath(fileName);
        logger.info("Р¤Р°Р№Р» '{}' РёР· classpath Р·Р°РіСЂСѓР¶РµРЅ РІ СЌР»РµРјРµРЅС‚: {}", fileName, name);
        return this;
    }

    /**
     * Р—Р°РіСЂСѓР·РєР° РЅРµСЃРєРѕР»СЊРєРёС… С„Р°Р№Р»РѕРІ РёР· classpath
     */
    @Step("Р—Р°РіСЂСѓР¶Р°РµРј С„Р°Р№Р»С‹ РёР· classpath РІ СЌР»РµРјРµРЅС‚: {name}")
    public SmartElement uploadFromClasspath(String... fileNames) {
        logger.debug("Р—Р°РіСЂСѓР¶Р°РµРј {} С„Р°Р№Р»РѕРІ РёР· classpath РІ СЌР»РµРјРµРЅС‚: {}", fileNames.length, name);
        waitStrategy.waitFor(element, name);
        element.uploadFromClasspath(fileNames);
        logger.info("Р¤Р°Р№Р»С‹ РёР· classpath Р·Р°РіСЂСѓР¶РµРЅС‹ РІ СЌР»РµРјРµРЅС‚: {}", name);
        return this;
    }

    /**
     * Р’С‹РїРѕР»РЅРµРЅРёРµ JavaScript
     */
    public Object executeJavaScript(String script, Object... args) {
        logger.debug("Р’С‹РїРѕР»РЅСЏРµРј JavaScript РІ СЌР»РµРјРµРЅС‚Рµ: {}", name);
        return com.bft.pw.Selenide.executeJavaScript(script, args);
    }

    /**
     * РџРѕР»СѓС‡РµРЅРёРµ SelenideElement (РґР»СЏ РѕР±СЂР°С‚РЅРѕР№ СЃРѕРІРјРµСЃС‚РёРјРѕСЃС‚Рё)
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