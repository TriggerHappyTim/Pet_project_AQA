package com.bft.pw;

/**
 * Фабрика селекторов, совместимая с {@code org.openqa.selenium.By}.
 */
public class By {

    private final String selector;
    private final boolean xpath;

    private By(String selector, boolean xpath) {
        this.selector = selector;
        this.xpath = xpath;
    }

    public String getSelector() {
        return selector;
    }

    public String toPlaywrightSelector() {
        return xpath ? "xpath=" + selector : selector;
    }

    @Override
    public String toString() {
        return (xpath ? "By.xpath: " : "By.cssSelector: ") + selector;
    }

    // ================= Стандартные селекторы =================

    public static By id(String id) {
        return new By("#" + id, false);
    }

    public static By cssSelector(String cssSelector) {
        return new By(cssSelector, false);
    }

    public static By xpath(String xpath) {
        return new By(xpath, true);
    }

    public static By className(String className) {
        return new By("." + className, false);
    }

    public static By tagName(String tagName) {
        return new By(tagName, false);
    }

    public static By name(String name) {
        return new By("[name='" + name + "']", false);
    }

    public static By linkText(String linkText) {
        return new By("text=" + linkText, false);
    }

    public static By partialLinkText(String partialLinkText) {
        return new By("text=" + partialLinkText, false);
    }
}