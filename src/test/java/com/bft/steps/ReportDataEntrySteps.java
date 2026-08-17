package com.bft.steps;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

@Component
public class ReportDataEntrySteps {

    // ... ваши существующие методы ...

    /**
     * Заполняет поля «Начало периода» и «Конец периода» в форме периода стажа.
     * <p>Использует JavaScript для надежного ввода в MUI masked input.
     *
     * @param startDate дата начала в формате DD.MM.YYYY
     * @param endDate   дата окончания в формате DD.MM.YYYY
     * @return текущий экземпляр шагов для цепочки вызовов
     */
    @Step("Заполнение периода стажа: с {startDate} по {endDate}")
    public ReportDataEntrySteps inputDatePeriodStaj(String startDate, String endDate) {
        // Находим диалог или форму, где находятся поля периода
        // Предполагаем, что мы уже находимся в нужной модалке или разделе

        try {
            // Ждем появления полей по ID (как было в MainPage)
            var dialog = $x("//*[@role='dialog'][.//input[@id='experienceTimePeriodDateAt']]")
                    .shouldBe(Condition.visible, Duration.ofSeconds(10));

            var byIdStart = dialog.$("#experienceTimePeriodDateAt");
            var byIdEnd = dialog.$("#experienceTimePeriodDateTo");

            byIdStart.shouldBe(Condition.visible, Duration.ofSeconds(5));
            byIdEnd.shouldBe(Condition.visible, Duration.ofSeconds(5));

            setDateViaJsReact(byIdStart, startDate);
            setDateViaJsReact(byIdEnd, endDate);

            return this;
        } catch (Exception e) {
            // Fallback: попытка найти по лейблам, если ID не сработали
            inputDateByLabelContains("Начало периода", startDate);
            inputDateByLabelContains("Конец периода", endDate);
            return this;
        }
    }

    /**
     * Вспомогательный метод для установки даты через JS (React controlled input)
     */
    private void setDateViaJsReact(SelenideElement input, String date) {
        Selenide.executeJavaScript(
                "var el = arguments[0]; var v = arguments[1];" +
                        "var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                        "if (setter) setter.call(el, v); else el.value = v;" +
                        "el.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "el.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "el.dispatchEvent(new Event('blur', { bubbles: true }));",
                input.getWrappedElement(),
                date
        );
    }

    /**
     * Вспомогательный метод для ввода даты по части текста лейбла
     */
    private void inputDateByLabelContains(String labelPart, String date) {
        String escaped = labelPart.replace("'", "''");
        var input = $x("//div[contains(., '" + escaped + "')]//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(5));

        Selenide.executeJavaScript(
                "arguments[0].value = arguments[1]; " +
                        "arguments[0].dispatchEvent(new Event('input', { bubbles: true })); " +
                        "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                input.getWrappedElement(),
                date
        );
    }
}