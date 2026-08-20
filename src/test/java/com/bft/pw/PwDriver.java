package com.bft.pw;

import com.bft.pw.logging.PwLogs;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Обёртка над Playwright {@link Page}, имитирующая {@code WebDriver}.
 */
public class PwDriver {

    private final Page page;

    public PwDriver(Page page) {
        this.page = page;
    }

    public Page page() {
        return page;
    }

    public String getCurrentUrl() {
        return page.url();
    }

    public String getTitle() {
        return page.title();
    }

    public String getPageSource() {
        return page.content();
    }

    public PwOptions manage() {
        return new PwOptions(page);
    }

    public PwElement findElement(By by) {
        return new PwElement(page.locator(by.toPlaywrightSelector()));
    }

    public List<PwElement> findElements(By by) {
        LocatorLite locator = new LocatorLite(page.locator(by.toPlaywrightSelector()));
        List<PwElement> result = new ArrayList<>();
        int count = locator.count();
        for (int i = 0; i < count; i++) {
            result.add(new PwElement(page.locator(by.toPlaywrightSelector()).nth(i)));
        }
        return result;
    }

    public Object executeScript(String script, Object... args) {
        Object[] mapped = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof PwElement) {
                mapped[i] = ((PwElement) args[i]).locator().elementHandle();
            } else {
                mapped[i] = args[i];
            }
        }
        if (mapped.length == 0) {
            return page.evaluate(script);
        }
        return page.evaluate(script, mapped);
    }

    public PwTargetLocator switchTo() {
        return new PwTargetLocator();
    }

    public PwNavigation navigate() {
        return new PwNavigation(page);
    }

    public void get(String url) {
        page.navigate(url);
    }

    public void close() {
        page.close();
    }

    // Минимальная обёртка для упрощения findElements
    private static final class LocatorLite {
        private final com.microsoft.playwright.Locator locator;

        LocatorLite(com.microsoft.playwright.Locator locator) {
            this.locator = locator;
        }

        int count() {
            return locator.count();
        }
    }

    public static final class PwNavigation {
        private final Page page;

        public PwNavigation(Page page) {
            this.page = page;
        }

        public void to(String url) {
            page.navigate(url);
        }

        public void refresh() {
            page.reload();
        }

        public void back() {
            page.goBack();
        }

        public void forward() {
            page.goForward();
        }
    }
}