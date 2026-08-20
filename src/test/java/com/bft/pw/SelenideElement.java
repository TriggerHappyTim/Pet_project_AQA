package com.bft.pw;

import java.io.File;
import java.time.Duration;
import java.util.List;

/**
 * Поверхность API, совместимая с Selenide {@code SelenideElement}.
 */
public interface SelenideElement {

    // ================= Поиск внутри элемента =================

    SelenideElement $(String cssSelector);

    SelenideElement $(By selector);

    SelenideElement $x(String xpath);

    SelenideElement find(String cssSelector);

    SelenideElement find(By selector);

    ElementsCollection $$(String cssSelector);

    ElementsCollection $$(By selector);

    ElementsCollection $$x(String xpath);

    List<SelenideElement> findElements(By selector);

    SelenideElement as(String alias);

    SelenideElement parent();

    SelenideElement closest(String cssSelector);

    // ================= Состояние =================

    boolean exists();

    boolean isDisplayed();

    boolean is(Condition condition);

    boolean isEnabled();

    boolean isSelected();

    boolean isChecked();

    boolean isEditable();

    boolean isFocused();

    int count();

    // ================= Ожидания =================

    SelenideElement shouldBe(Condition condition);

    SelenideElement shouldBe(Condition condition, Duration timeout);

    SelenideElement should(Condition condition);

    SelenideElement should(Condition condition, Duration timeout);

    SelenideElement shouldHave(Condition condition);

    SelenideElement shouldHave(Condition condition, Duration timeout);

    SelenideElement shouldNotBe(Condition condition);

    SelenideElement shouldNotBe(Condition condition, Duration timeout);

    SelenideElement shouldNotHave(Condition condition);

    SelenideElement shouldNotHave(Condition condition, Duration timeout);

    SelenideElement shouldNot(Condition condition);

    SelenideElement shouldNot(Condition condition, Duration timeout);

    SelenideElement waitUntil(Condition condition, Duration timeout);

    SelenideElement waitWhile(Condition condition, Duration timeout);

    // ================= Действия =================

    SelenideElement click();

    SelenideElement doubleClick();

    SelenideElement hover();

    SelenideElement setValue(String text);

    SelenideElement append(String text);

    SelenideElement clear();

    SelenideElement sendKeys(CharSequence... keys);

    SelenideElement pressEnter();

    SelenideElement pressTab();

    SelenideElement pressEscape();

    SelenideElement press(String key);

    SelenideElement selectOption(String text);

    SelenideElement selectOptionContainingText(String text);

    SelenideElement selectOptionByValue(String value);

    SelenideElement scrollIntoView(boolean alignToTop);

    SelenideElement scrollTo();

    SelenideElement uploadFile(File file);

    SelenideElement uploadFile(File... files);

    SelenideElement uploadFromClasspath(String fileName);

    SelenideElement uploadFromClasspath(String... fileNames);

    SelenideElement setSelected(boolean selected);

    SelenideElement focus();

    // ================= Чтение значений =================

    String getText();

    String text();

    String innerText();

    String getValue();

    String val();

    String getAttribute(String name);

    String getCssValue(String propertyName);

    boolean isImage();

    byte[] screenshot();

    // ================= JavaScript =================

    Object executeJavaScript(String script, Object... args);

    // ================= Внутренние объекты =================

    Object getWrappedElement();

    PwDriver getWrappedDriver();

    SelenideElement getSelenideElement();

    Actions actions();
}