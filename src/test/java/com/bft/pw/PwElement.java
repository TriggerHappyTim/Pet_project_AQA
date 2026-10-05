package com.bft.pw;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Обёртка над Playwright {@link Locator}, реализующая Selenide-совместимый API.
 */
public class PwElement implements SelenideElement {

    private static final long DEFAULT_POLLING_MS = 100L;

    private final Locator locator;
    private String alias;

    public PwElement(Locator locator) {
        this.locator = locator;
    }

    public Locator locator() {
        return locator;
    }

    ElementHandle elementHandle() {
        try {
            return locator.elementHandle();
        } catch (Exception e) {
            return null;
        }
    }

    private Page page() {
        return PwSession.page();
    }

    // ================= Поиск внутри элемента =================

    @Override
    public SelenideElement $(String cssSelector) {
        return new PwElement(locator.locator(cssSelector));
    }

    @Override
    public SelenideElement $(By selector) {
        return new PwElement(locator.locator(selector.toPlaywrightSelector()));
    }

    @Override
    public SelenideElement $x(String xpath) {
        return new PwElement(locator.locator("xpath=" + xpath));
    }

    @Override
    public SelenideElement find(String cssSelector) {
        return $(cssSelector);
    }

    @Override
    public SelenideElement find(By selector) {
        return $(selector);
    }

    @Override
    public ElementsCollection $$(String cssSelector) {
        return new PwElements(locator.locator(cssSelector));
    }

    @Override
    public ElementsCollection $$(By selector) {
        return new PwElements(locator.locator(selector.toPlaywrightSelector()));
    }

    @Override
    public ElementsCollection $$x(String xpath) {
        return new PwElements(locator.locator("xpath=" + xpath));
    }

    @Override
    public List<SelenideElement> findElements(By selector) {
        List<SelenideElement> result = new ArrayList<>();
        Locator children = locator.locator(selector.toPlaywrightSelector());
        int count = children.count();
        for (int i = 0; i < count; i++) {
            result.add(new PwElement(children.nth(i)));
        }
        return result;
    }

    @Override
    public SelenideElement as(String alias) {
        this.alias = alias;
        return this;
    }

    @Override
    public SelenideElement parent() {
        return new PwElement(locator.locator("xpath=.."));
    }

    @Override
    public SelenideElement closest(String cssSelector) {
        return new PwElement(locator.locator("xpath=ancestor::" + cssSelector + "[1]"));
    }

    // ================= Состояние =================

    @Override
    public int count() {
        return locator.count();
    }

    @Override
    public boolean exists() {
        return count() > 0;
    }

    @Override
    public boolean isDisplayed() {
        return isVisible();
    }

    public boolean isVisible() {
        if (!exists()) {
            return false;
        }
        try {
            // Selenide-семантика: $x(...) описывает первый элемент коллекции.
            // Playwright isVisible() на мульти-локаторе бросает strict mode violation,
            // поэтому явно берём first().
            return locator.first().isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        if (!exists()) {
            return false;
        }
        try {
            return locator.first().isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isSelected() {
        return isChecked();
    }

    @Override
    public boolean isChecked() {
        try {
            return locator.isChecked();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isEditable() {
        try {
            return locator.isEditable();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isFocused() {
        ElementHandle handle = elementHandle();
        if (handle == null) {
            return false;
        }
        try {
            Object res = PwSession.evaluate(page(), "document.activeElement === arguments[0]", handle);
            return Boolean.TRUE.equals(res);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean is(Condition condition) {
        try {
            return condition.check(this);
        } catch (Exception e) {
            return false;
        }
    }

    // ================= Ожидания =================

    @Override
    public SelenideElement shouldBe(Condition condition) {
        return waitFor(condition, Configuration.timeout, true);
    }

    @Override
    public SelenideElement shouldBe(Condition condition, Duration timeout) {
        return waitFor(condition, timeout, true);
    }

    @Override
    public SelenideElement should(Condition condition) {
        return shouldBe(condition);
    }

    @Override
    public SelenideElement should(Condition condition, Duration timeout) {
        return shouldBe(condition, timeout);
    }

    @Override
    public SelenideElement shouldHave(Condition condition) {
        return shouldBe(condition);
    }

    @Override
    public SelenideElement shouldHave(Condition condition, Duration timeout) {
        return shouldBe(condition, timeout);
    }

    @Override
    public SelenideElement shouldNotBe(Condition condition) {
        return waitFor(condition, Configuration.timeout, false);
    }

    @Override
    public SelenideElement shouldNotBe(Condition condition, Duration timeout) {
        return waitFor(condition, timeout, false);
    }

    @Override
    public SelenideElement shouldNotHave(Condition condition) {
        return shouldNotBe(condition);
    }

    @Override
    public SelenideElement shouldNotHave(Condition condition, Duration timeout) {
        return shouldNotBe(condition, timeout);
    }

    @Override
    public SelenideElement shouldNot(Condition condition) {
        return shouldNotBe(condition);
    }

    @Override
    public SelenideElement shouldNot(Condition condition, Duration timeout) {
        return shouldNotBe(condition, timeout);
    }

    @Override
    public SelenideElement waitUntil(Condition condition, Duration timeout) {
        return waitFor(condition, timeout, true);
    }

    @Override
    public SelenideElement waitWhile(Condition condition, Duration timeout) {
        return waitFor(condition, timeout, false);
    }

    private SelenideElement waitFor(Condition condition, Duration timeout, boolean expected) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        Throwable lastError = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                if (condition.check(this) == expected) {
                    return this;
                }
            } catch (Throwable t) {
                lastError = t;
            }
            try {
                Thread.sleep(DEFAULT_POLLING_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        String expectedDesc = expected ? "satisfy" : "NOT satisfy";
        try {
            WebDriverRunner.saveSourceDump("shouldBe-" + expectedDesc + "-" + condition);
        } catch (Exception ignored) {
            // диагностический дамп не должен маскировать исходную ошибку
        }
        throw new TimeoutException(String.format(
                "Element %s did not %s condition '%s' within %s",
                describe(), expectedDesc, condition, timeout));
    }

    // ================= Действия =================

    @Override
    public SelenideElement click() {
        try {
            locator.click();
            return this;
        } catch (Throwable t) {
            WebDriverRunner.saveSourceDump("action-click-fail");
            throw t;
        }
    }

    @Override
    public SelenideElement doubleClick() {
        locator.dblclick();
        return this;
    }

    @Override
    public SelenideElement hover() {
        locator.hover();
        return this;
    }

    @Override
    public SelenideElement setValue(String text) {
        try {
            locator.fill(text);
            return this;
        } catch (Throwable t) {
            WebDriverRunner.saveSourceDump("action-fill-fail");
            throw t;
        }
    }

    @Override
    public SelenideElement append(String text) {
        click();
        locator.pressSequentially(text);
        return this;
    }

    @Override
    public SelenideElement clear() {
        try {
            locator.fill("");
        } catch (Exception e) {
            try {
                locator.clear();
            } catch (Exception ignored) {
                // пропускаем очистку, если элемент не редактируемый
            }
        }
        return this;
    }

    @Override
    public SelenideElement sendKeys(CharSequence... keys) {
        StringBuilder plain = new StringBuilder();
        List<String> special = new ArrayList<>();
        for (CharSequence cs : keys) {
            String s = cs.toString();
            for (int i = 0; i < s.length(); i++) {
                String ch = String.valueOf(s.charAt(i));
                String mapped = Keys.toPlaywrightKey(ch);
                if (mapped != null) {
                    special.add(mapped);
                } else {
                    plain.append(ch);
                }
            }
        }
        boolean hasModifier = special.contains("Control") || special.contains("Alt")
                || special.contains("Shift") || special.contains("Meta");
        if (hasModifier && plain.length() > 0) {
            for (String m : special) {
                if (isModifier(m)) {
                    page().keyboard().down(m);
                }
            }
            locator.pressSequentially(plain.toString());
            for (String m : special) {
                if (isModifier(m)) {
                    page().keyboard().up(m);
                }
            }
        } else {
            for (String sp : special) {
                locator.press(sp);
            }
            if (plain.length() > 0) {
                locator.pressSequentially(plain.toString());
            }
        }
        return this;
    }

    private static boolean isModifier(String key) {
        return "Control".equals(key) || "Alt".equals(key)
                || "Shift".equals(key) || "Meta".equals(key);
    }

    @Override
    public SelenideElement pressEnter() {
        locator.press("Enter");
        return this;
    }

    @Override
    public SelenideElement pressTab() {
        locator.press("Tab");
        return this;
    }

    @Override
    public SelenideElement pressEscape() {
        locator.press("Escape");
        return this;
    }

    @Override
    public SelenideElement press(String key) {
        locator.press(key);
        return this;
    }

    @Override
    public SelenideElement selectOption(String text) {
        locator.selectOption(new SelectOption().setLabel(text));
        return this;
    }

    @Override
    public SelenideElement selectOptionContainingText(String text) {
        Locator option = locator.locator("option").filter(
                new Locator.FilterOptions().setHasText(text)).first();
        String label = option.getAttribute("label");
        if (label == null || label.isEmpty()) {
            label = option.innerText();
        }
        locator.selectOption(new SelectOption().setLabel(label));
        return this;
    }

    @Override
    public SelenideElement selectOptionByValue(String value) {
        locator.selectOption(value);
        return this;
    }

    @Override
    public SelenideElement scrollIntoView(boolean alignToTop) {
        locator.scrollIntoViewIfNeeded();
        return this;
    }

    @Override
    public SelenideElement scrollTo() {
        locator.scrollIntoViewIfNeeded();
        return this;
    }

    @Override
    public SelenideElement uploadFile(File file) {
        locator.setInputFiles(file.toPath());
        return this;
    }

    @Override
    public SelenideElement uploadFile(File... files) {
        Path[] paths = new Path[files.length];
        for (int i = 0; i < files.length; i++) {
            paths[i] = files[i].toPath();
        }
        locator.setInputFiles(paths);
        return this;
    }

    @Override
    public SelenideElement uploadFromClasspath(String fileName) {
        File temp = copyClasspathResource(fileName);
        if (temp != null) {
            locator.setInputFiles(temp.toPath());
        }
        return this;
    }

    @Override
    public SelenideElement uploadFromClasspath(String... fileNames) {
        Path[] paths = new Path[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            File temp = copyClasspathResource(fileNames[i]);
            paths[i] = temp != null ? temp.toPath() : Paths.get("");
        }
        locator.setInputFiles(paths);
        return this;
    }

    private static File copyClasspathResource(String fileName) {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(fileName)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found in classpath: " + fileName);
            }
            Path temp = Files.createTempFile("upload-", fileName.substring(fileName.lastIndexOf('.')));
            try (OutputStream out = Files.newOutputStream(temp)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            return temp.toFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy classpath resource: " + fileName, e);
        }
    }

    @Override
    public SelenideElement setSelected(boolean selected) {
        if (selected && !isChecked()) {
            click();
        } else if (!selected && isChecked()) {
            click();
        }
        return this;
    }

    @Override
    public SelenideElement focus() {
        locator.focus();
        return this;
    }

    // ================= Чтение значений =================

    @Override
    public String getText() {
        if (!exists()) {
            return "";
        }
        try {
            return locator.innerText();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String text() {
        return getText();
    }

    @Override
    public String innerText() {
        return getText();
    }

    @Override
    public String getValue() {
        String value = getAttribute("value");
        return value != null ? value : "";
    }

    @Override
    public String val() {
        return getValue();
    }

    @Override
    public String getAttribute(String name) {
        if (!exists()) {
            return null;
        }
        try {
            return locator.getAttribute(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getCssValue(String propertyName) {
        ElementHandle handle = elementHandle();
        if (handle == null) {
            return "";
        }
        try {
            Object res = page().evaluate(
                    "([el, prop]) => getComputedStyle(el).getPropertyValue(prop)",
                    java.util.Arrays.asList(handle, propertyName));
            return res != null ? res.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public boolean isImage() {
        return "img".equalsIgnoreCase(getAttribute("tagName"));
    }

    @Override
    public byte[] screenshot() {
        return locator.screenshot();
    }

    // ================= JavaScript =================

    @Override
    public Object executeJavaScript(String script, Object... args) {
        Object[] mapped = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof PwElement) {
                mapped[i] = ((PwElement) args[i]).elementHandle();
            } else {
                mapped[i] = args[i];
            }
        }
        return PwSession.evaluate(page(), script, mapped);
    }

    // ================= Внутренние объекты =================

    @Override
    public Object getWrappedElement() {
        return this;
    }

    @Override
    public PwDriver getWrappedDriver() {
        return PwSession.driver();
    }

    @Override
    public SelenideElement getSelenideElement() {
        return this;
    }

    @Override
    public Actions actions() {
        return new Actions(PwSession.driver());
    }

    private String describe() {
        return alias != null ? "'" + alias + "'" : locator.toString();
    }

    @Override
    public String toString() {
        return alias != null ? "PwElement('" + alias + "')" : "PwElement(" + locator + ")";
    }
}