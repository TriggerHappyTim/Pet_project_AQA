package com.bft.ui.core;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.ElementClickInterceptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Утилита для устойчивых кликов по элементам.
 *
 * <p>Стандартный клик Selenide может падать с {@link ElementClickInterceptedException},
 * когда элемент в момент клика перекрыт другим элементом (оверлей, тост, лоадер).
 * Утилита прокручивает элемент к центру и при перехвате клика выполняет JS-клик
 * как fallback — тот же паттерн, что уже использовался в MainPage.
 */
public final class ClickHelper {

    private static final Logger logger = LoggerFactory.getLogger(ClickHelper.class);

    private ClickHelper() {
    }

    /**
     * Устойчивый клик: scrollIntoView + клик, при перехвате — JS-клик.
     *
     * @param element     элемент для клика
     * @param elementName читаемое имя элемента для логирования
     */
    public static void click(SelenideElement element, String elementName) {
        try {
            element.scrollIntoView(true);
            element.click();
        } catch (Throwable e) {
            if (hasCause(e, ElementClickInterceptedException.class)) {
                logger.warn("Клик по '{}' перехвачен другим элементом, выполняем JS-клик: {}",
                    elementName, e.getMessage());
                Selenide.executeJavaScript("arguments[0].click();", element.getWrappedElement());
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof Error) {
                throw (Error) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean hasCause(Throwable throwable, Class<?> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}