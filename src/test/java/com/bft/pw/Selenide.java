package com.bft.pw;

/**
 * Статический фасад, совместимый с Selenide {@code Selenide}.
 */
public final class Selenide {

    private Selenide() {
    }

    public static SelenideElement $(String cssSelector) {
        return new PwElement(PwSession.page().locator(cssSelector));
    }

    public static SelenideElement $(By by) {
        return new PwElement(PwSession.page().locator(by.toPlaywrightSelector()));
    }

    public static SelenideElement $x(String xpath) {
        return new PwElement(PwSession.page().locator("xpath=" + xpath));
    }

    public static ElementsCollection $$(String cssSelector) {
        return new PwElements(PwSession.page().locator(cssSelector));
    }

    public static ElementsCollection $$(By by) {
        return new PwElements(PwSession.page().locator(by.toPlaywrightSelector()));
    }

    public static ElementsCollection $$x(String xpath) {
        return new PwElements(PwSession.page().locator("xpath=" + xpath));
    }

    public static void open(String url) {
        PwSession.page().navigate(url);
    }

    public static void open() {
        PwSession.page().navigate("about:blank");
    }

    public static void refresh() {
        PwSession.page().reload();
    }

    public static void back() {
        PwSession.page().goBack();
    }

    public static void forward() {
        PwSession.page().goForward();
    }

    public static void sleep(long millis) {
        PwSession.sleep(millis);
    }

    public static Object executeJavaScript(String script, Object... args) {
        return PwSession.executeJavaScript(script, args);
    }

    public static String screenshot(String name) {
        return PwSession.screenshot(name);
    }

    public static void confirm() {
        PwSession.confirm();
    }

    public static void dismiss() {
        PwSession.dismiss();
    }

    public static void closeWebDriver() {
        PwSession.close();
    }

    public static PwTargetLocator switchTo() {
        return new PwTargetLocator();
    }

    public static Actions actions() {
        return new Actions(PwSession.driver());
    }

    public static Wait<PwDriver> Wait() {
        return new Wait<>();
    }
}