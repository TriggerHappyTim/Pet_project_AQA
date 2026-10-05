package com.bft.ui.core;

import com.bft.pw.Selenide;
import com.bft.pw.SelenideElement;
import com.microsoft.playwright.PlaywrightException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Утилита для устойчивых кликов по элементам.
 *
 * <p>Стандартный клик Playwright может падать, когда элемент в момент клика перекрыт
 * другим элементом (оверлей, тост, лоадер) или не готов к взаимодействию.
 * Утилита прокручивает элемент к центру и при ошибке выполняет JS-клик
 * как fallback — тот же паттерн, что использовался в MainPage.
 */
public final class ClickHelper {

    private static final Logger logger = LoggerFactory.getLogger(ClickHelper.class);

    private ClickHelper() {
    }

    /**
     * Устойчивый клик: scrollIntoView + клик, при ошибке — JS-клик.
     *
     * @param element     элемент для клика
     * @param elementName читаемое имя элемента для логирования
     */
    /**
     * Устойчивый клик: scrollIntoView + клик, при перекрытии sticky-панелью —
     * повторный скролл к нижней границе, при ошибке — JS-клик с повторным
     * поиском кнопки по тексту (без устаревших JS-handle).
     *
     * @param element     элемент для клика
     * @param elementName читаемое имя элемента для логирования (может содержать
     *                    текст кнопки в одинарных кавычках)
     */
    public static void click(SelenideElement element, String elementName) {
        try {
            element.click();
            return;
        } catch (Throwable e) {
            if (!hasCause(e, PlaywrightException.class)) {
                rethrow(e);
                return;
            }
            logger.warn("Клик по '{}' не удался: {}, пробуем повторно со скроллом",
                elementName, e.getMessage());
        }
        try {
            scrollIntoViewEnd(element);
            element.click();
            return;
        } catch (Throwable e) {
            if (!hasCause(e, PlaywrightException.class)) {
                rethrow(e);
                return;
            }
            logger.warn("Клик по '{}' не удался и после повторного скролла: {}, JS-клик",
                elementName, e.getMessage());
        }
        try {
            String buttonText = extractButtonText(elementName);
            if (buttonText != null) {
                jsClickByText(buttonText);
                return;
            }
        } catch (Throwable ignored) {
            logger.warn("JS-клик по '{}' не выполнен", elementName);
        }
        element.click();
    }

    private static void scrollIntoViewEnd(SelenideElement element) {
        Selenide.executeJavaScript(
            "arguments[0].scrollIntoView({block:'end', inline:'nearest'});", element);
    }

    /**
     * Извлекает текст кнопки из имени элемента («Кнопка 'Сохранить' #2» -> «Сохранить»).
     */
    private static String extractButtonText(String elementName) {
        if (elementName == null) {
            return null;
        }
        int from = elementName.indexOf('\'');
        int to = elementName.lastIndexOf('\'');
        if (from >= 0 && to > from) {
            return elementName.substring(from + 1, to);
        }
        return null;
    }

    /**
     * JS-клик по кнопке с заданным текстом без проверки видимости и без ожиданий.
     * Ищет кнопку заново в текущем DOM. Используется для кнопок, которые
     * присутствуют в DOM, но не проходят стандартное условие видимости.
     *
     * @param buttonText точный текст на кнопке (обязательно внутри span)
     * @throws com.microsoft.playwright.PlaywrightException если кнопка не найдена или disabled
     */
    public static void clickByText(String buttonText) {
        jsClickByText(buttonText);
    }

    /**
     * JS-клик по последней кнопке (btn-primary/btn-secondary/link) с заданным текстом.
     * Используется как последний резерв: ищет элемент заново в текущем DOM клиентского
     * скрипта, поэтому не зависит от устаревших JS-handle и не проверяет перекрытие.
     */
    private static void jsClickByText(String buttonText) {
        boolean clicked = (Boolean) Selenide.executeJavaScript(
            "var spans = Array.prototype.slice.call(document.querySelectorAll('span'));"
            + "var found = null;"
            + "for (var i = 0; i < spans.length; i++) {"
            + "  if (spans[i].textContent.trim() === '" + buttonText + "') { found = spans[i]; }"
            + "}"
            + "if (found) { var b = found.closest('button'); if (b && !b.disabled) { b.click(); return true; } }"
            + "return false;");
        if (!clicked) {
            throw new PlaywrightException("JS-клик по кнопке '" + buttonText + "' не выполнен");
        }
    }

    private static void rethrow(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        throw new RuntimeException(throwable);
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