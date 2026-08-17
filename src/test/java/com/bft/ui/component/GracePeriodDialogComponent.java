package com.bft.ui.component;

import com.bft.enums.TimeoutConstants;
import com.bft.utils.DebugUtils;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.$$x;

/**
 * Компонент для работы с диалогом формы "Льготный стаж".
 * Инкапсулирует сложную логику поиска диалога, заполнения полей и обработки React-перерисовок.
 */
public class GracePeriodDialogComponent {

    private static final Logger log = LoggerFactory.getLogger(GracePeriodDialogComponent.class);
    private static final String INPUT_NAME_TU_BASIS = "tuBasis";
    private static final String DIALOG_TITLE_PART = "ЛЬГОТНЫЙ СТАЖ";

    private final SelenideElement dialog;

    /**
     * Конструктор. Находит активный диалог "Льготный стаж".
     * Если диалог не найден, выбрасывает ошибку с дампом страницы.
     */
    public GracePeriodDialogComponent() {
        this.dialog = findActiveDialog();
    }

    /**
     * Поиск диалога с надежной стратегией.
     */
    private SelenideElement findActiveDialog() {
        try {
            // Стратегия 1: Поиск по полю tuBasis (самый надежный признак формы строки стажа)
            var tuBasisInput = $("input[name='" + INPUT_NAME_TU_BASIS + "']");
            if (tuBasisInput.exists() && tuBasisInput.isDisplayed()) {
                return tuBasisInput.$x("./ancestor::*[@role='dialog'][1]");
            }

            // Стратегия 2: Поиск по заголовку или содержанию
            var dialogByTitle = $x("//*[@role='dialog'][contains(., '" + DIALOG_TITLE_PART + "')]");
            if (dialogByTitle.exists() && dialogByTitle.isDisplayed()) {
                return dialogByTitle;
            }

            // Fallback: последний открытый диалог
            var dialogs = $$x("//*[@role='dialog']");
            if (!dialogs.isEmpty()) {
                return dialogs.get(dialogs.size() - 1);
            }

            throw new AssertionError("Диалог 'Льготный стаж' не найден на странице.");

        } catch (Exception e) {
            DebugUtils.savePageStateOnError("grace-period-dialog-not-found", e);
            throw new AssertionError("Не удалось найти диалог формы 'Льготный стаж'. Структура сохранена в target/debug.", e);
        }
    }

    /**
     * Заполняет даты периода (Начало и Конец) через JS (для React controlled inputs).
     */
    @io.qameta.allure.Step("Заполнение дат периода: {start} - {end}")
    public GracePeriodDialogComponent fillPeriodDates(String start, String end) {
        log.debug("Заполнение дат периода: {} - {}", start, end);

        // Ищем поля по ID внутри диалога
        var startField = dialog.$("#experienceTimePeriodDateAt");
        var endField = dialog.$("#experienceTimePeriodDateTo");

        if (!startField.exists() || !endField.exists()) {
            // Альтернативный поиск, если ID изменились
            startField = dialog.$x(".//input[contains(@id, 'DateAt') or contains(@placeholder, 'Начало')]");
            endField = dialog.$x(".//input[contains(@id, 'DateTo') or contains(@placeholder, 'Конец')]");
        }

        startField.shouldBe(Condition.visible, Duration.ofSeconds(5));
        endField.shouldBe(Condition.visible, Duration.ofSeconds(5));

        setInputValueViaJs(startField, start);
        setInputValueViaJs(endField, end);

        waitForStabilization();
        return this;
    }

    /**
     * Кликает кнопку "Добавить" внутри таблицы диалога и ждет появления новой формы строки.
     */
    @io.qameta.allure.Step("Нажатие кнопки 'Добавить' в таблице льготного стажа")
    public GracePeriodDialogComponent clickAddInTable() {
        log.debug("Клик по кнопке 'Добавить' в таблице...");

        var addBtn = dialog.$x(".//button[span[text()='Добавить']] | .//button[contains(text(), 'Добавить')]")
                .shouldBe(Condition.visible, Duration.ofSeconds(5));

        int dialogsBefore = $$x("//*[@role='dialog']").size();
        addBtn.click();

        // Ждем появления НОВОГО диалога (формы строки)
        waitForNewDialogAppearance(dialogsBefore);

        return this;
    }

    /**
     * Выбирает значение в селекте по имени поля (name) внутри формы.
     */
    @io.qameta.allure.Step("Выбор значения '{value}' в поле '{fieldName}'")
    public GracePeriodDialogComponent selectInFormByName(String fieldName, String value) {
        var input = dialog.$("input[name='" + fieldName + "']");
        if (!input.exists()) {
            throw new AssertionError("Поле с name='" + fieldName + "' не найдено в диалоге.");
        }
        input.shouldBe(Condition.visible, Duration.ofSeconds(5)).click();
        clickDropdownOptionByText(value);
        waitForStabilization();
        return this;
    }

    /**
     * Универсальный метод выбора: сначала по name, если нет - перебор по меткам.
     */
    @io.qameta.allure.Step("Выбор значения '{value}' (поле: {fieldName}, метки: {labels})")
    public GracePeriodDialogComponent selectByFieldOrLabel(String fieldName, String[] labels, String value) {
        var input = dialog.$("input[name='" + fieldName + "']");
        if (input.exists() && input.isDisplayed()) {
            return selectInFormByName(fieldName, value);
        }
        return selectByLabelVariants(labels, value);
    }

    /**
     * Выбор значения перебором вариантов меток.
     */
    @io.qameta.allure.Step("Выбор значения '{value}' по одному из вариантов меток")
    public GracePeriodDialogComponent selectByLabelVariants(String[] labels, String value) {
        for (String label : labels) {
            try {
                selectByLabel(label, value);
                return this;
            } catch (Exception ignored) {
                // Пробуем следующую метку
            }
        }
        throw new AssertionError("Не удалось найти селект по меткам: " + String.join(", ", labels));
    }

    /**
     * Ввод значения: сначала по name, если нет - перебор по меткам.
     */
    @io.qameta.allure.Step("Ввод значения '{value}' (поле: {fieldName}, метки: {labels})")
    public GracePeriodDialogComponent inputByFieldOrLabel(String fieldName, String[] labels, String value) {
        var input = dialog.$("input[name='" + fieldName + "']");
        if (input.exists() && input.isDisplayed()) {
            setInputValueViaJs(input, value);
            return this;
        }
        return inputByLabelVariants(labels, value);
    }

    /**
     * Ввод значения перебором вариантов меток.
     */
    @io.qameta.allure.Step("Ввод значения '{value}' по одному из вариантов меток")
    public GracePeriodDialogComponent inputByLabelVariants(String[] labels, String value) {
        for (String label : labels) {
            try {
                inputByLabel(label, value);
                return this;
            } catch (Exception ignored) {
                // Пробуем следующую метку
            }
        }
        throw new AssertionError("Не удалось найти поле по меткам: " + String.join(", ", labels));
    }

    /**
     * Заполняет Коэффициент и Долю ставки (специфичная логика для этой формы).
     */
    @io.qameta.allure.Step("Заполнение коэффициента ({coef}) и доли ставки ({share})")
    public GracePeriodDialogComponent fillCoefficientAndBidShare(String coef, String share) {
        // Попытка по name
        try {
            var coefInput = dialog.$("input[name='tuCoefficient']");
            var shareInput = dialog.$("input[name='tuBidShare']");
            if (coefInput.isDisplayed() && shareInput.isDisplayed()) {
                setInputValueViaJs(coefInput, coef);
                setInputValueViaJs(shareInput, share);
                return this;
            }
        } catch (Exception ignored) {}

        // Fallback по меткам
        inputByLabelVariants(new String[]{"Коэффициент", "Доля ставки", "Коэф"}, coef);
        inputByLabelVariants(new String[]{"Доля ставки", "Коэффициент", "Доля"}, share);
        return this;
    }

    /**
     * Сохраняет форму строки.
     */
    @io.qameta.allure.Step("Сохранение формы строки льготного стажа")
    public void clickSaveInForm() {
        dialog.$x(".//button[span[text()='Сохранить']] | .//button[contains(text(), 'Сохранить')]")
                .shouldBe(Condition.visible, Duration.ofSeconds(5))
                .click();

        // Ждем закрытия формы строки (возврат к родительскому диалогу)
        sleep(500);
    }

    // --- Приватные вспомогательные методы ---

    private void selectByLabel(String label, String value) {
        String escaped = label.replace("'", "''");
        var trigger = dialog.$x(".//*[contains(., '" + escaped + "')]/ancestor::*[contains(@class,'MuiFormControl')]//input[not(@type='hidden')] | .//*[contains(., '" + escaped + "')]/following::input[not(@type='hidden')][1]");

        if (!trigger.exists()) {
            throw new AssertionError("Не найден input для метки: " + label);
        }

        trigger.shouldBe(Condition.visible, Duration.ofSeconds(3)).click();
        clickDropdownOptionByText(value);
        waitForStabilization();
    }

    private void inputByLabel(String label, String value) {
        String escaped = label.replace("'", "''");
        var input = dialog.$x(".//*[contains(., '" + escaped + "')]/ancestor::*[contains(@class,'MuiFormControl')]//input[not(@type='hidden')] | .//*[contains(., '" + escaped + "')]/following::input[not(@type='hidden')][1]");

        if (!input.exists()) {
            throw new AssertionError("Не найден input для метки: " + label);
        }

        input.shouldBe(Condition.visible, Duration.ofSeconds(3));
        setInputValueViaJs(input, value);
    }

    private void clickDropdownOptionByText(String text) {
        String escaped = text.replace("'", "''");
        String[] xpaths = {
                "//li[contains(@id, 'option') and contains(., '" + escaped + "')]",
                "//*[@role='option'][contains(., '" + escaped + "')]"
        };

        for (String xpath : xpaths) {
            try {
                var el = $x(xpath);
                el.shouldBe(Condition.visible, Duration.ofSeconds(5));
                el.scrollIntoView(true);
                sleep(100);
                el.click();
                return;
            } catch (Exception ignored) {}
        }
        throw new AssertionError("Опция '" + text + "' не найдена в списке.");
    }

    private void waitForNewDialogAppearance(int countBefore) {
        long timeout = TimeoutConstants.DEFAULT_WAIT.toMillis();
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeout) {
            int currentCount = $$x("//*[@role='dialog']").size();
            if (currentCount > countBefore) {
                // Новый диалог появился
                sleep(300); // Даем React отрисоваться
                return;
            }
            sleep(200);
        }
        DebugUtils.savePageStateOnError("new-dialog-did-not-appear", null);
        throw new AssertionError("Новый диалог не появился после клика 'Добавить'.");
    }

    private void waitForStabilization() {
        try {
            dialog.shouldBe(Condition.visible, Duration.ofSeconds(2));
            dialog.$x(".//*[contains(@class,'MuiCircularProgress')]").should(Condition.disappear, Duration.ofSeconds(3));
            sleep(300);
        } catch (Exception e) {
            DebugUtils.savePageStateOnError("dialog-instability", e);
            throw new AssertionError("Диалог не стабилизировался.", e);
        }
    }

    private void setInputValueViaJs(SelenideElement input, String value) {
        executeJavaScript(
                "var el = arguments[0]; var v = arguments[1];" +
                        "var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                        "if (setter) setter.call(el, v); else el.value = v;" +
                        "el.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "el.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "el.dispatchEvent(new Event('blur', { bubbles: true }));",
                input.getWrappedElement(), value
        );
    }
}