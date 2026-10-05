package com.bft.ui.component;

import com.bft.ui.core.element.ElementFactory;
import com.bft.pw.By;

/**
 * Компонент для работы с кнопками различных типов в интерфейсе EVS
 * 
 * Предоставляет унифицированный интерфейс для взаимодействия с кнопками:
 * - Обычные кнопки
 * - Кнопки в футере, хедере, модальных окнах
 * - Первичные (btn-primary) и вторичные (btn-secondary) кнопки
 * - Кнопки с текстом внутри span элементов
 * 
 * <p>Все factory методы статические и возвращают готовый к использованию компонент.
 * Автоматическое ожидание кликабельности кнопки перед взаимодействием.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Простой клик по кнопке
 * ButtonComponent.createButton("Сохранить").click();
 * 
 * // Клик по кнопке в футере
 * ButtonComponent.createFooterButton("Отправить").click();
 * 
 * // Клик по первичной кнопке
 * ButtonComponent.createPrimaryButton("Подтвердить").click();
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see BaseComponent для базовой функциональности
 * @since 1.0
 */
public class ButtonComponent extends BaseComponent {

    /**
     * Создает компонент кнопки с CSS селектором
     * 
     * @param buttonSelector CSS селектор кнопки
     * @param buttonName читаемое имя кнопки для логирования
     */
    public ButtonComponent(String buttonSelector, String buttonName) {
        super(ElementFactory.css(buttonSelector).named(buttonName).waitClickable().build(), buttonName);
    }

    /**
     * Создает компонент кнопки с By локатором
     * 
     * Рекомендуемый способ создания компонента, так как By.xpath
     * более универсален для сложных селекторов.
     * 
     * @param buttonLocator By локатор кнопки (обычно XPath)
     * @param buttonName читаемое имя кнопки для логирования
     */
    public ButtonComponent(By buttonLocator, String buttonName) {
        super(ElementFactory.by(buttonLocator).named(buttonName).waitClickable().build(), buttonName);
    }

    /**
     * Проверяет валидность состояния кнопки
     * 
     * Кнопка валидна если:
     * - Присутствует в DOM
     * - Видима на странице
     * - Доступна для клика (enabled)
     * 
     * @return true если кнопка в валидном состоянии, false в противном случае
     */
    @Override
    public boolean isValid() {
        return isPresent() && isVisible() && isEnabled();
    }

    /**
     * Выполняет клик по кнопке
     * 
     * Автоматически ожидает кликабельность кнопки перед выполнением клика.
     * Логирует действие для отладки.
     * 
     * @return текущий экземпляр ButtonComponent для цепочки вызовов (fluent API)
     * @throws com.bft.pw.ex.ElementShould если кнопка не стала кликабельной
     */
    public ButtonComponent click() {
        logger.info("Кликаем по кнопке: {}", componentName);
        rootElement.click();
        return this;
    }

    // ===== СТАТИЧЕСКИЕ FACTORY МЕТОДЫ ДЛЯ РАЗЛИЧНЫХ ТИПОВ КНОПОК =====

    /**
     * Создает компонент для стандартной кнопки в приложении
     * 
     * Ищет кнопку внутри секции с классом 'application' по тексту внутри дочерних элементов.
     * 
     * @param buttonName текст на кнопке (например, "Сохранить", "Добавить")
     * @return новый экземпляр ButtonComponent
     */
    public static ButtonComponent createButton(String buttonName) {
        return new ButtonComponent(
            By.xpath("//section[contains(@class, 'application')]//button[*[text() = '" + buttonName
                    + "'] or normalize-space(text()) = '" + buttonName + "']"),
            "Кнопка '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для кнопки в футере страницы
     * 
     * Используется для кнопок в нижней части страницы (футер формы, модального окна).
     * 
     * @param buttonName точный текст на кнопке
     * @return новый экземпляр ButtonComponent
     */
    public static ButtonComponent createFooterButton(String buttonName) {
        return new ButtonComponent(
            By.xpath("//div[@class = 'item-footer']//button[text() = '" + buttonName + "']"),
            "Кнопка футера '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для кнопки в футере с текстом внутри span элемента
     * 
     * Используется когда текст кнопки обернут в span.
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @return новый экземпляр ButtonComponent
     */
    public static ButtonComponent createFooterSpanButton(String buttonName) {
        return new ButtonComponent(
            By.xpath("//div[@class = 'item-footer']//button[span[text() = '" + buttonName + "']]"),
            "Кнопка футера span '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для кликабельного span элемента
     * 
     * Используется когда кнопка реализована через span вместо button.
     * Поиск по частичному совпадению текста (contains).
     * 
     * @param buttonName текст содержащийся в span (частичное совпадение)
     * @return новый экземпляр ButtonComponent
     */
    public static ButtonComponent createSpanButton(String buttonName) {
        return new ButtonComponent(
            By.xpath("//span[contains(text(), '" + buttonName + "')]"),
            "Span кнопка '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для главной кнопки по частичному совпадению текста
     * 
     * Ищет кнопку в любом месте страницы по содержанию текста.
     * 
     * @param buttonName текст содержащийся в кнопке (частичное совпадение)
     * @return новый экземпляр ButtonComponent
     */
    public static ButtonComponent createMainButton(String buttonName) {
        return new ButtonComponent(
            By.xpath("//button[contains(text(), '" + buttonName + "')]"),
            "Главная кнопка '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для второй кнопки с одинаковым текстом
     * 
     * Используется когда на странице несколько кнопок с одинаковым текстом.
     * Выбирает вторую по порядку кнопку в DOM.
     * 
     * @param buttonName текст на кнопке
     * @return новый экземпляр ButtonComponent для второй кнопки
     */
    public static ButtonComponent createSecondButton(String buttonName) {
        return new ButtonComponent(
            By.xpath("(//section[contains(@class, 'application')]//button[*[text() = '" + buttonName + "']])[2]"),
            "Вторая кнопка '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для третьей кнопки с одинаковым текстом
     * 
     * Выбирает четвертый span с указанным текстом (индекс [4] в XPath).
     * 
     * @param buttonName текст на кнопке
     * @return новый экземпляр ButtonComponent для третьей кнопки
     */
    public static ButtonComponent createThirdButton(String buttonName) {
        return new ButtonComponent(
            By.xpath("(//span[text() = '" + buttonName + "'])[4]"),
            "Третья кнопка '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для кнопки в модальном окне
     * 
     * Ищет кнопку внутри элемента с классом 'modal-content'.
     * 
     * @param buttonName текст на кнопке внутри модального окна
     * @return новый экземпляр ButtonComponent
     */
    public static ButtonComponent createModalButton(String buttonName) {
        return new ButtonComponent(
            By.xpath("//div[@class = 'modal-content']//button[*[text() = '" + buttonName + "']]"),
            "Кнопка модального окна '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для первичной кнопки (btn-primary)
     * 
     * Первичные кнопки обычно используются для главного действия на странице.
     * Имеют CSS класс 'btn-primary'.
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @return новый экземпляр ButtonComponent
     */
    public static ButtonComponent createPrimaryButton(String buttonName) {
        return new ButtonComponent(
            By.xpath("//button[contains(@class, 'btn-primary')]//span[text() = '" + buttonName + "']"),
            "Первичная кнопка '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для первичной кнопки по индексу
     * 
     * Универсальный метод для создания первичной кнопки с указанным индексом.
     * Индекс начинается с 1 (первая кнопка = 1, вторая = 2 и т.д.).
     * 
     * <p>Пример использования:
     * <pre>{@code
     * // Первая первичная кнопка "Сохранить"
     * ButtonComponent.createPrimaryButtonByIndex("Сохранить", 1).click();
     * 
     * // Вторая первичная кнопка "Отправить"
     * ButtonComponent.createPrimaryButtonByIndex("Отправить", 2).click();
     * }</pre>
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @param index индекс кнопки (начинается с 1)
     * @return новый экземпляр ButtonComponent
     * @throws IllegalArgumentException если index меньше 1
     */
    public static ButtonComponent createPrimaryButtonByIndex(String buttonName, int index) {
        if (index < 1) {
            throw new IllegalArgumentException("Index must be >= 1, but was: " + index);
        }
        return new ButtonComponent(
            By.xpath("(//button[contains(@class, 'btn-primary')]//span[text() = '" + buttonName + "'])[" + index + "]"),
            "Первичная кнопка '" + buttonName + "' #" + index
        );
    }

    /**
     * Создает компонент для первой первичной кнопки
     * 
     * @deprecated Используйте {@link #createPrimaryButtonByIndex(String, int)} с index=1
     * @param buttonName текст внутри span элемента кнопки
     * @return новый экземпляр ButtonComponent
     */
    @Deprecated
    public static ButtonComponent createPrimaryButtonFirst(String buttonName) {
        return createPrimaryButtonByIndex(buttonName, 1);
    }

    /**
     * Создает компонент для второй первичной кнопки
     * 
     * @deprecated Используйте {@link #createPrimaryButtonByIndex(String, int)} с index=2
     * @param buttonName текст внутри span элемента кнопки
     * @return новый экземпляр ButtonComponent
     */
    @Deprecated
    public static ButtonComponent createPrimaryButtonSecond(String buttonName) {
        return createPrimaryButtonByIndex(buttonName, 2);
    }

    /**
     * Создает компонент для вторичной кнопки (btn-secondary)
     * 
     * Вторичные кнопки используются для дополнительных действий на странице.
     * Имеют CSS класс 'btn-secondary'.
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @return новый экземпляр ButtonComponent
     */
    public static ButtonComponent createSecondaryButton(String buttonName) {
        return new ButtonComponent(
            By.xpath("//button[contains(@class, 'btn-secondary')]//span[text() = '" + buttonName + "']"),
            "Вторичная кнопка '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для вторичной кнопки по индексу
     * 
     * Универсальный метод для создания вторичной кнопки с указанным индексом.
     * Индекс начинается с 1 (первая кнопка = 1, вторая = 2 и т.д.).
     * 
     * <p>Пример использования:
     * <pre>{@code
     * // Первая вторичная кнопка "Отмена"
     * ButtonComponent.createSecondaryButtonByIndex("Отмена", 1).click();
     * 
     * // Вторая вторичная кнопка "Удалить"
     * ButtonComponent.createSecondaryButtonByIndex("Удалить", 2).click();
     * }</pre>
     * 
     * @param buttonName текст внутри span элемента кнопки
     * @param index индекс кнопки (начинается с 1)
     * @return новый экземпляр ButtonComponent
     * @throws IllegalArgumentException если index меньше 1
     */
    public static ButtonComponent createSecondaryButtonByIndex(String buttonName, int index) {
        if (index < 1) {
            throw new IllegalArgumentException("Index must be >= 1, but was: " + index);
        }
        return new ButtonComponent(
            By.xpath("(//button[contains(@class, 'btn-secondary')]//span[text() = '" + buttonName + "'])[" + index + "]"),
            "Вторичная кнопка '" + buttonName + "' #" + index
        );
    }

    /**
     * Создает компонент для второй вторичной кнопки
     * 
     * @deprecated Используйте {@link #createSecondaryButtonByIndex(String, int)} с index=2
     * @param buttonName текст внутри span элемента кнопки
     * @return новый экземпляр ButtonComponent
     */
    @Deprecated
    public static ButtonComponent createSecondaryButtonSecond(String buttonName) {
        return createSecondaryButtonByIndex(buttonName, 2);
    }

    /**
     * Создает компонент для третьей вторичной кнопки
     * 
     * @deprecated Используйте {@link #createSecondaryButtonByIndex(String, int)} с index=3
     * @param buttonName текст внутри span элемента кнопки
     * @return новый экземпляр ButtonComponent
     */
    @Deprecated
    public static ButtonComponent createSecondaryButtonThird(String buttonName) {
        return createSecondaryButtonByIndex(buttonName, 3);
    }

    /**
     * Создает компонент для четвертой вторичной кнопки
     * 
     * @deprecated Используйте {@link #createSecondaryButtonByIndex(String, int)} с index=4
     * @param buttonName текст внутри span элемента кнопки
     * @return новый экземпляр ButtonComponent
     */
    @Deprecated
    public static ButtonComponent createSecondaryButtonFourth(String buttonName) {
        return createSecondaryButtonByIndex(buttonName, 4);
    }

    /**
     * Создает компонент для пятой вторичной кнопки
     * 
     * @deprecated Используйте {@link #createSecondaryButtonByIndex(String, int)} с index=5
     * @param buttonName текст внутри span элемента кнопки
     * @return новый экземпляр ButtonComponent
     */
    @Deprecated
    public static ButtonComponent createSecondaryButtonFifth(String buttonName) {
        return createSecondaryButtonByIndex(buttonName, 5);
    }

    /**
     * Создает компонент для шестой вторичной кнопки
     * 
     * @deprecated Используйте {@link #createSecondaryButtonByIndex(String, int)} с index=6
     * @param buttonName текст внутри span элемента кнопки
     * @return новый экземпляр ButtonComponent
     */
    /**
     * Создает компонент для шестой вторичной кнопки
     * 
     * @deprecated Используйте {@link #createSecondaryButtonByIndex(String, int)} с index=6
     * @param buttonName текст внутри span элемента кнопки
     * @return новый экземпляр ButtonComponent
     */
    @Deprecated
    public static ButtonComponent createSecondaryButtonSixth(String buttonName) {
        return createSecondaryButtonByIndex(buttonName, 6);
    }

    /**
     * Создает компонент для вторичной кнопки в новом окне (вторая вкладка)
     */
    public static ButtonComponent createSecondaryButtonNewWindow(String buttonName) {
        return new ButtonComponent(
            By.xpath("(//button[contains(@class, 'btn-secondary')]//span)[1]"),
            "Вторичная кнопка в новом окне '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для простой кнопки
     */
    public static ButtonComponent createSimpleButton(String buttonName) {
        return new ButtonComponent(
            By.xpath("//button[text() = '" + buttonName + "']"),
            "Простая кнопка '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для кнопки модального диалога
     */
    public static ButtonComponent createModalDialogButton(String buttonName) {
        return new ButtonComponent(
            By.xpath("//div[@class = 'modal-dialog']//button[text() = '" + buttonName + "']"),
            "Кнопка модального диалога '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для кнопки закрытия диалога
     */
    public static ButtonComponent createCloseDialogButton(String buttonName) {
        return new ButtonComponent(
            By.xpath("//div[@class = 'close-dialog']//button[text() = '" + buttonName + "']"),
            "Кнопка закрытия диалога '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для span кнопки закрытия диалога
     */
    public static ButtonComponent createCloseDialogSpanButton(String buttonName) {
        return new ButtonComponent(
            By.xpath("//div[@class = 'close-dialog']//span[text() = '" + buttonName + "']"),
            "Span кнопка закрытия диалога '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для кнопки диалога провайдера
     */
    public static ButtonComponent createProviderDialogButton(String buttonName) {
        return new ButtonComponent(
            By.xpath("//div[@class = 'provider-dialog']//button[text() = '" + buttonName + "']"),
            "Кнопка диалога провайдера '" + buttonName + "'"
        );
    }

    /**
     * Создает компонент для кнопки с произвольным XPath селектором
     * 
     * Используйте когда стандартные factory методы не подходят.
     * Позволяет указать любой XPath для поиска кнопки.
     * 
     * <p>Пример:
     * <pre>{@code
     * ButtonComponent customButton = ButtonComponent.createCustomButton(
     *     "//div[@id='custom']//button[@data-test='submit']",
     *     "Кастомная кнопка отправки"
     * );
     * customButton.click();
     * }</pre>
     * 
     * @param selector XPath селектор для поиска кнопки
     * @param buttonName читаемое имя кнопки для логирования
     * @return новый экземпляр ButtonComponent
     */
    public static ButtonComponent createCustomButton(String selector, String buttonName) {
        return new ButtonComponent(By.xpath(selector), buttonName);
    }
}