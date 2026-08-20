package com.bft.pw;

import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Page;

import java.util.List;

/**
 * Имитация {@code WebDriver.TargetLocator}.
 */
public class PwTargetLocator {

    public PwDriver window(int index) {
        List<Page> pages = PwSession.context().pages();
        if (index >= pages.size()) {
            throw new IndexOutOfBoundsException("No window with index " + index
                    + " (available: " + pages.size() + ")");
        }
        Page target = pages.get(index);
        target.bringToFront();
        PwSession.setActivePage(target);
        return new PwDriver(target);
    }

    public PwDriver window(String nameOrHandle) {
        List<Page> pages = PwSession.context().pages();
        for (Page p : pages) {
            if (nameOrHandle != null && nameOrHandle.equals(p.url())) {
                p.bringToFront();
                PwSession.setActivePage(p);
                return new PwDriver(p);
            }
        }
        return window(pages.size() - 1);
    }

    public PwDriver defaultContent() {
        return new PwDriver(PwSession.page());
    }

    public PwDriver parentFrame() {
        return new PwDriver(PwSession.page());
    }

    public PwAlert alert() {
        return new PwAlert();
    }

    public static final class PwAlert {
        public void accept() {
            PwSession.confirm();
        }

        public void dismiss() {
            PwSession.dismiss();
        }

        public String getText() {
            Dialog dialog = PwSession.lastDialog();
            return dialog != null ? dialog.message() : "";
        }

        public void sendKeys(String keysToSend) {
            Dialog dialog = PwSession.lastDialog();
            if (dialog != null) {
                dialog.accept(keysToSend);
            }
        }
    }
}