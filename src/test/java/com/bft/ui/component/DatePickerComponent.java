package com.bft.ui.component;

import com.bft.utils.DebugUtils;
import com.bft.pw.Condition;
import com.bft.pw.SelenideElement;
import com.bft.pw.Keys;
import com.bft.pw.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.bft.pw.Selenide.$;
import static com.bft.pw.Selenide.$x;
import static com.bft.pw.Selenide.$$x;
import static com.bft.pw.Selenide.sleep;
import static com.bft.pw.WebDriverRunner.getWebDriver;

/**
 * Компонент для работы с полями дат (DatePicker, masked input, JS-ввод).
 *
 * <p>Содержит методы установки дат в формы EVS: через компонент {@link DateComponent},
 * через календарь MUI и через JavaScript (для React controlled input / masked date).
 *
 * <p>Пример использования:
 * <pre>{@code
 * DatePickerComponent datePicker = new DatePickerComponent();
 * datePicker.inputDateByLabel("Начало периода", "01.01.2025");
 * datePicker.inputDateEventDateViaJs("01.01.2025");
 * }</pre>
 */
public class DatePickerComponent {

    private static final Logger log = LoggerFactory.getLogger(DatePickerComponent.class);

    private static final String INPUT_NAME_TU_BASIS = "tuBasis";
    private static final String DEBUG_OUTPUT_DIR = "target/debug/";

    /**
     * Устанавливает дату в поле по ID.
     *
     * @param value   ID поля даты
     * @param content дата в формате строки (например, "01-01-2000")
     * @return текущий экземпляр DatePickerComponent для цепочки вызовов
     */
    public DatePickerComponent inputDateLabel(String value, String content) {
        DateComponent.createIdDate(value, value).setDate(content);
        return this;
    }

    /**
     * Устанавливает дату в поле по тексту метки (label).
     * Использует поиск по подстроке (contains), чтобы находить поле при метке в span/MUI и лишних пробелах.
     *
     * @param label текст метки поля (например, "Дата мероприятия")
     * @param date  дата в формате DD.MM.YYYY или DD-MM-YYYY
     * @return текущий экземпляр DatePickerComponent для цепочки вызовов
     */
    public DatePickerComponent inputDateByLabel(String label, String date) {
        DateComponent.createLabeledDateContains(label, label).setDate(date);
        return this;
    }

    /**
     * Устанавливает дату в поле «Дата мероприятия» через календарь MUI (клик по полю → выбор дня в календаре).
     * Используется когда прямое ввод в input не обновляет состояние компонента (MUI DatePicker).
     *
     * @param dateDDMMYYYY дата в формате DD.MM.YYYY (например, "01.01.2025")
     * @return текущий экземпляр DatePickerComponent для цепочки вызовов
     */
    public DatePickerComponent inputDateEventDateViaCalendar(String dateDDMMYYYY) {
        String[] parts = dateDDMMYYYY.replace("-", ".").split("\\.");
        int day = parts.length > 0 ? Integer.parseInt(parts[0].trim()) : 1;
        int year = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 2025;

        $x("//input[@id='eventDate']").shouldBe(Condition.visible, Duration.ofSeconds(10)).click();
        $("[role=dialog]").shouldBe(Condition.visible, Duration.ofSeconds(10));

        String yearStr = String.valueOf(year);
        for (int i = 0; i < 24; i++) {
            String header = $("[role=dialog]").getText().toLowerCase();
            if (header.contains(yearStr) && (header.contains("январ") || header.contains("january"))) {
                break;
            }
            boolean clicked = false;
            if ($("[role=dialog]").$x(".//button[contains(@aria-label, 'revious') or contains(@aria-label, 'Previous') or contains(@aria-label, 'редыдущ') or contains(@aria-label, 'ack') or contains(@aria-label, 'efore')]").exists()) {
                $("[role=dialog]").$x(".//button[contains(@aria-label, 'revious') or contains(@aria-label, 'Previous') or contains(@aria-label, 'редыдущ') or contains(@aria-label, 'ack') or contains(@aria-label, 'efore')]").click();
                clicked = true;
            }
            if (!clicked && $("[role=dialog]").$x(".//div[contains(@class,'CalendarHeader')]//button[1]").exists()) {
                $("[role=dialog]").$x(".//div[contains(@class,'CalendarHeader')]//button[1]").click();
                clicked = true;
            }
            if (!clicked && $("[role=dialog]").$x(".//button[.//*[local-name()='svg']][1]").exists()) {
                $("[role=dialog]").$x(".//button[.//*[local-name()='svg']][1]").click();
                clicked = true;
            }
            if (!clicked) {
                break;
            }
            $x("//body").shouldBe(Condition.visible, Duration.ofMillis(400));
        }

        $("[role=dialog]").$x(".//*[@role='gridcell' and text()='" + day + "']").shouldBe(Condition.visible, Duration.ofSeconds(5)).click();
        return this;
    }

    /**
     * Устанавливает дату в поле «Дата мероприятия» (#eventDate) через JavaScript.
     * Подходит для MUI masked input (mask 00.00.0000, delimiter "."), когда календарь ненадёжен.
     *
     * @param dateDDMMYYYY дата в формате DD.MM.YYYY (например, "01.01.2025")
     * @return текущий экземпляр DatePickerComponent для цепочки вызовов
     */
    public DatePickerComponent inputDateEventDateViaJs(String dateDDMMYYYY) {
        inputDateByIdViaJs("eventDate", dateDDMMYYYY);
        return this;
    }

    /**
     * Устанавливает дату в поле по id инпута через JavaScript (MUI masked date).
     * Используется когда ввод через календарь не применяет значение (например «Конец периода»).
     *
     * @param inputId      id элемента input (например, "experienceTimePeriodDateTo")
     * @param dateDDMMYYYY дата в формате DD.MM.YYYY
     * @return текущий экземпляр DatePickerComponent для цепочки вызовов
     */
    public DatePickerComponent inputDateByIdViaJs(String inputId, String dateDDMMYYYY) {
        com.bft.pw.Selenide.executeJavaScript(
            "var el = document.getElementById(arguments[0]) || document.querySelector('input[name=\"' + arguments[0] + '\"]'); if (el) { el.value = arguments[1]; el.dispatchEvent(new Event('input', { bubbles: true })); el.dispatchEvent(new Event('change', { bubbles: true })); }",
            inputId,
            dateDDMMYYYY
        );
        return this;
    }

    /**
     * Устанавливает дату в поле по тексту метки: ищет input через Selenide (XPath по подстроке метки), затем значение — через JS.
     * Подходит для MUI, когда чисто JS не находит элемент (метка в span и т.п.).
     *
     * @param labelText    текст метки (например, "Начало периода", "Конец периода")
     * @param dateDDMMYYYY дата в формате DD.MM.YYYY
     * @return текущий экземпляр DatePickerComponent для цепочки вызовов
     */
    public DatePickerComponent inputDateByLabelTextViaJs(String labelText, String dateDDMMYYYY) {
        var input = $x("//div[.//*[contains(., '" + labelText.replace("'", "''") + "')]]//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));
        com.bft.pw.Selenide.executeJavaScript(
            "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input', { bubbles: true })); arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            input.getWrappedElement(),
            dateDDMMYYYY
        );
        return this;
    }

    /**
     * Возвращает диалог формы «Льготный стаж», открытый после клика «Добавить» в таблице.
     * Ищет НОВЫЙ диалог с формой строки (не родительский диалог "Сведения о льготном периоде").
     * Форма строки открывается в отдельном диалоге с полем tuBasis.
     * При ошибке поиска парсит структуру страницы для отладки.
     */
    private SelenideElement getGracePeriodFormDialog() {
        try {
            // АЛЬТЕРНАТИВНАЯ СТРАТЕГИЯ 1: Ищем поле tuBasis глобально на странице (в новом диалоге)
            // Это самый надёжный способ - форма строки всегда содержит это поле
            for (int attempt = 0; attempt < 30; attempt++) {
                try {
                    var tuBasisInput = $("input[name='" + INPUT_NAME_TU_BASIS + "']");
                    if (tuBasisInput.exists() && tuBasisInput.isDisplayed()) {
                        // Нашли поле tuBasis - ищем его родительский диалог
                        var dialog = tuBasisInput.$x("./ancestor::*[@role='dialog'][1]");
                        if (dialog.exists() && dialog.isDisplayed()) {
                            log.debug("Найден диалог формы строки по полю tuBasis");
                            return dialog;
                        }
                        // Если не нашли по role='dialog', пробуем по классу modal
                        dialog = tuBasisInput.$x("./ancestor::*[contains(@class,'modal')][1]");
                        if (dialog.exists() && dialog.isDisplayed()) {
                            log.debug("Найден диалог формы строки по полю tuBasis (через класс modal)");
                            return dialog;
                        }
                    }
                } catch (Exception ignored) {
                    // Продолжаем поиск
                }

                sleep(300);
            }

            // АЛЬТЕРНАТИВНАЯ СТРАТЕГИЯ 2: Ищем все открытые диалоги и проверяем наличие tuBasis в каждом
            try {
                var allDialogs = $$x("//*[@role='dialog']");
                for (var dialog : allDialogs) {
                    try {
                        var tuBasisInDialog = dialog.$x(".//input[@name='" + INPUT_NAME_TU_BASIS + "']");
                        if (tuBasisInDialog.exists() && tuBasisInDialog.isDisplayed()) {
                            log.debug("Найден диалог формы строки среди открытых диалогов");
                            return dialog;
                        }
                    } catch (Exception ignored) {
                        // Продолжаем поиск в следующем диалоге
                    }
                }
            } catch (Exception ignored) {
                // Продолжаем к следующей стратегии
            }

            // АЛЬТЕРНАТИВНАЯ СТРАТЕГИЯ 3: Используем самый последний открытый диалог как fallback
            try {
                var allDialogs = $$x("//*[@role='dialog']");
                if (allDialogs.size() > 0) {
                    var lastDialog = allDialogs.get(allDialogs.size() - 1);
                    log.debug("Используем последний открытый диалог как fallback");
                    return lastDialog;
                }
            } catch (Exception ignored) {
                // Продолжаем к финальной проверке
            }

            // Если ничего не найдено, пробрасываем ошибку
            throw new AssertionError("Не удалось найти диалог с формой строки (поле tuBasis не найдено)");

        } catch (Throwable e) {
            // Если не удалось найти диалог, парсим структуру страницы для отладки
            log.warn("========================================");
            log.warn("ОШИБКА: Не найден диалог формы строки 'Льготный стаж' (input[name='tuBasis'])");
            log.warn("Парсим структуру страницы для анализа...");
            log.warn("========================================");

            DebugUtils.savePageStateOnError("grace-period-failure", e);

            // Пробрасываем исходное исключение с информацией о сохранённых данных
            throw new AssertionError("Не найден диалог формы строки 'Льготный стаж'. " +
                "Элемент input[name='tuBasis'] не найден на странице. " +
                "Возможно, форма строки не открылась после клика 'Добавить' в таблице. " +
                "Структура страницы сохранена в " + DEBUG_OUTPUT_DIR + " для анализа. " +
                "Оригинальная ошибка: " + e.getMessage(), e);
        }
    }

    /**
     * Устанавливает значение в React controlled input через нативный setter и события,
     * чтобы React обновил state (setValue часто не срабатывает для MUI/числовых полей).
     */
    private void setInputValueViaJs(SelenideElement input, String value) {
        com.bft.pw.Selenide.executeJavaScript(
            "var el = arguments[0]; var v = arguments[1];"
            + "var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;"
            + "if (setter) setter.call(el, v); else el.value = v;"
            + "el.dispatchEvent(new Event('input', { bubbles: true }));"
            + "el.dispatchEvent(new Event('change', { bubbles: true }));"
            + "el.dispatchEvent(new Event('blur', { bubbles: true }));",
            input.getWrappedElement(),
            value
        );
    }

    /**
     * Отправляет клавишу Escape (закрывает открытый календарь/попап).
     *
     * @return текущий экземпляр DatePickerComponent для цепочки вызовов
     */
    public DatePickerComponent pressEscape() {
        new Actions(getWebDriver()).sendKeys(Keys.ESCAPE).perform();
        return this;
    }
}