package com.bft.LK_Insurence.EFS_1;

import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
import com.bft.security.TestUsers;
import com.bft.steps.*;
import com.bft.test.base.UITestBase;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;
import static java.time.Duration.ofSeconds;

/**
 * Тесты для отчёта ЕФС-1 (Единая форма сведений)
 */
@Epic("Формы отчетности")
@Feature("EFS-1 Reports")
public class Efs1 extends UITestBase {

    private final AuthSteps authSteps = new AuthSteps();
    private final ReportNavigationSteps reportNavSteps = new ReportNavigationSteps();
    private final ReportDataEntrySteps dataSteps = new ReportDataEntrySteps();
    private final ArchiveSteps archiveSteps = new ArchiveSteps();
    private final SzvReportsSteps legacySteps = new SzvReportsSteps();

    // ========== XML Загрузка ==========

    @Test(groups = {"web", "efs", "smoke", "xml-upload", "user-specific"},
            testName = "#4 Загрузка отчёта ЕФС-1 через XML (Кривоносов А.П.)",
            description = "Загрузка отчёта ЕФС-1 через XML от пользователя Кривоносов")
    @AllureId("EFS-004")
    @Story("XML Upload - Кривоносов А.П.")
    @Description("Тест проверяет загрузку отчёта ЕФС-1 через XML от пользователя Кривоносов Александр Петрович")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_1_xml_krivonosov() {
        authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
        reportNavSteps.createNewReportDraft(ReportFormType.EFS1);
        legacySteps.sendXml(ReportXmlResource.EFS_FULL_ECP);
    }

    @Test(groups = {"web", "efs", "smoke", "xml-upload", "user-specific"},
            description = "Загрузка отчёта ЕФС-1 через XML (Бабкина В.В.)")
    @Story("XML Upload - Бабкина В.В.")
    @Description("Тест проверяет загрузку отчёта ЕФС-1 через XML от пользователя Бабкина Вера Васильевна")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_1_xml_babkina() {
        authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.BABKINA_VERA);
        reportNavSteps.createNewReportDraft(ReportFormType.EFS1);
        legacySteps.sendXml(ReportXmlResource.EFS_FULL_ECP);
    }

    // ========== Ручное создание: ТД (Трудовая деятельность) ==========

    @Test(groups = {"web", "efs", "regression", "manual-creation"},
            testName = "#2 Ручное создание отчёта ЕФС-1 ТД",
            description = "Ручное создание отчёта ЕФС-1 ТД")
    @AllureId("EFS-002")
    @Story("Manual Creation - ТД")
    @Description("Тест проверяет ручное создание отчёта ЕФС-1 раздел 1.1 ТД (трудовая деятельность)")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_szv_td() {
        legacySteps.authorizeEVS(UITypeSelector.getSelectedUIType());
        legacySteps.addReports();
        legacySteps.selectReportType(ReportFormType.EFS1);
        legacySteps.addNewReport();
        legacySteps.addGeneralInfoEFS();
        legacySteps.createContinue();
        legacySteps.sidebar();
        legacySteps.addZL();
        legacySteps.fillZLEFS();
        legacySteps.addEventEFS();
        legacySteps.saveEvent();
        legacySteps.saveZL();
    }

    @Test(groups = {"web", "efs", "regression", "manual-creation", "user-specific"},
            testName = "#6 Ручное создание отчёта ЕФС-1 ТД (Кривоносов А.П.)",
            description = "Ручное создание отчёта ЕФС-1 ТД от пользователя Кривоносов")
    @AllureId("EFS-006")
    @Story("Manual Creation - ТД - Кривоносов А.П.")
    @Description("Тест проверяет ручное создание отчёта ЕФС-1 раздел 1.1 ТД от пользователя Кривоносов Александр Петрович")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_szv_td_krivonosov() {
        authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
        legacySteps.addReports();
        legacySteps.selectReportType(ReportFormType.EFS1);
        legacySteps.addNewReport();
        legacySteps.addGeneralInfoEFS();
        legacySteps.createContinue();
        legacySteps.sidebar();
        legacySteps.addZL();
        legacySteps.fillZLEFS();
        legacySteps.addEventEFS();
        legacySteps.saveEvent();
        legacySteps.saveZL();
    }

    // ========== Ручное создание: СТАЖ ==========

    @Test(groups = {"web", "efs", "regression", "manual-creation"},
            description = "Ручное создание отчёта ЕФС-1 раздел 1.2 СТАЖ")
    @Story("Manual Creation - СТАЖ")
    @Description("Тест проверяет ручное создание отчёта ЕФС-1 раздел 1.2 СТАЖ: ЗЛ, период стажа, сохранение.")
    @Severity(SeverityLevel.NORMAL)
    @AllureId("EFS-STAJ-001")
    public void efs_szv_staj() {
        legacySteps.authorizeEVS(UITypeSelector.getSelectedUIType());
        legacySteps.addReports();
        legacySteps.selectReportType(ReportFormType.EFS1);
        legacySteps.addNewReport();
        legacySteps.addGeneralInfoEFS();
        legacySteps.createContinue();
        legacySteps.sidebar();
        legacySteps.addZL();
        legacySteps.fillZLEFS();
        legacySteps.addSTAJ();
        legacySteps.saveZL();

        /*// ИСПРАВЛЕНО: Проверяем отсутствие ошибок через shouldNotBe
        $x("//*[contains(text(),'Поле обязательно для заполнения')]")
                .shouldNotBe(exist, ofSeconds(2));

        // Проверка наличия записи в таблице
        $x("//tbody[contains(@class, 'n2o-table-tbody')]//tr[1]")
                .shouldBe(visible, ofSeconds(10));*/
    }
}