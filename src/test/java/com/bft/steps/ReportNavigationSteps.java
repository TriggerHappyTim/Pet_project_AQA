package com.bft.steps;

import com.bft.enums.ReportFormType;
import com.bft.security.masking.SecureLogger;
import com.bft.ui.pages.MainPage;
import com.bft.pw.Condition;
import io.qameta.allure.Step;
import org.springframework.stereotype.Component;

import static com.bft.enums.TimeoutConstants.DEFAULT_WAIT;
import static com.bft.enums.TimeoutConstants.SHORT_WAIT;
import static com.bft.pw.Selenide.$x;

/**
 * Шаги для навигации к разделу отчетов и создания черновика.
 */
@Component
public class ReportNavigationSteps {

    private final SecureLogger logger = SecureLogger.getLogger(getClass());

    @Step("Переход к разделу отчетов")
    public void navigateToReports() {
        new MainPage()
                .openTab("ЛК Страхователя")
                .openTab("Отчеты")
                .openTab("Список отчетов");
    }

@Step("Создание нового черновика отчета типа {reportType}")
    public void createNewReportDraft(ReportFormType reportType) {
        addReports();
        selectReportType(reportType);
        addNewReport();
    }

    @Step("Переход в реестр запросов (Архив)")
    public void openArchiveRequestsRegistry() {
        new MainPage()
                .openTab("ЛК Архивной Организации")
                .openTab("Реестр запросов")
                .openTab("Управление запросами");
    }

    @Step(value = "Добавление отчета")
    public void addReports() {
        new MainPage()
                .openTab("ЛК Страхователя")
                .openTab("Отчеты")
                .openTab("Список отчетов")
                .waitTableToLoad()
                .clickButton("Добавить отчет");
    }

    @Step(value = "Выбор типа отчета: {reportFormType}")
    public void selectReportType(ReportFormType reportFormType) {
        String reportTypeText = reportFormType.getDisplayName();
        String escaped = reportTypeText.replace("'", "''");
        logger.info("Выбираем тип отчета: {}", reportTypeText);

        com.bft.pw.Selenide.$x("//span[text() = '" + escaped + "']")
            .shouldBe(com.bft.pw.Condition.visible, java.time.Duration.ofSeconds(15));
        logger.debug("Модальное окно с типом отчета '{}' отображается", reportTypeText);

        com.bft.pw.SelenideElement labelToClick = com.bft.pw.Selenide.$x(
            "//label[contains(@class, 'n2o-radio-input')][.//span[text() = '" + escaped + "']]"
        );
        labelToClick.shouldBe(com.bft.pw.Condition.visible, java.time.Duration.ofSeconds(10));
        labelToClick.click();
        logger.info("Клик по label типа отчета '{}' выполнен", reportTypeText);

        try {
            com.bft.pw.Selenide.$x(
                "//label[contains(@class, 'n2o-radio-input')][contains(@class, 'checked')][.//span[text() = '" + escaped + "']]"
            ).shouldBe(com.bft.pw.Condition.visible, java.time.Duration.ofSeconds(3));
            logger.debug("Тип отчета '{}' отмечен как выбранный", reportTypeText);
        } catch (Exception e) {
            logger.debug("Класс checked не обнаружен, продолжаем (возможно, другой способ отображения выбора)");
        }

        new MainPage().clickBtnPrimary("Добавить");
        logger.info("Выбор типа отчета '{}' подтвержден", reportTypeText);
    }

    @Step(value = "Добавить новый отчет")
    public void addNewReport() {
        new MainPage()
                .clickBtnPrimary("Создать новый");
    }

    @Step(value = "Продолжить")
    public void createContinue() {
        $x("//span[contains(text(), 'Продолжить')]")
                .shouldBe(Condition.visible, DEFAULT_WAIT);
        new MainPage()
                .clickSpanButton("Продолжить")
                .clickBtn("Да");
        $x("//body").shouldBe(Condition.visible, SHORT_WAIT);
    }

    @Step(value = "Переход во владку ЗЛ")
    public void goToZL() {
        new MainPage()
                .clickSectionA("Застрахованные лица");
    }

    @Step(value = "Выбор раздела")
    public void sidebar() {
        new MainPage()
                .clickSidebar("sidebar__dropdown-title", "Раздел 1",
                        "sidebar__item inner", "1.1 ТД, 1.2 СТАЖ, 1.3 БЮДЖ");
    }
}