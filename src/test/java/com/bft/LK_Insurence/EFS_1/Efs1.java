package com.bft.LK_Insurence.EFS_1;

import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
import com.bft.security.TestUsers;
import com.bft.steps.ArchiveSteps;
import com.bft.steps.AuthSteps;
import com.bft.steps.ReportDataEntrySteps;
import com.bft.steps.ReportNavigationSteps;
import com.bft.steps.ReportValidationSteps;
import com.bft.steps.SzvReportsSteps;
import com.bft.test.base.UITestBase;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.bft.pw.Condition.exist;
import static com.bft.pw.Condition.visible;
import static com.bft.pw.Selenide.$x;
import static java.time.Duration.ofSeconds;

/**
 * Тесты для отчёта ЕФС-1 (Единая форма сведений)
 */
@Tag("lk-insurer")
@Tag("web")
@Tag("efs")
@Epic("Формы отчетности")
@Feature("EFS-1 Reports")
public class Efs1 extends UITestBase {

    private final AuthSteps authSteps = new AuthSteps();
    private final ReportNavigationSteps reportNavSteps = new ReportNavigationSteps();
    private final ReportDataEntrySteps dataSteps = new ReportDataEntrySteps();
    private final ReportValidationSteps validationSteps = new ReportValidationSteps();
    private final ArchiveSteps archiveSteps = new ArchiveSteps();
    private final SzvReportsSteps legacySteps = new SzvReportsSteps();

    // ========== XML Загрузка ==========

    @Test
    @AllureId("EFS-004")
    @Story("XML Upload - Кривоносов А.П.")
    @Description("Тест проверяет загрузку отчёта ЕФС-1 через XML от пользователя Кривоносов Александр Петрович")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_1_xml_krivonosov() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                reportNavSteps.createNewReportDraft(ReportFormType.EFS1);
                legacySteps.sendXml(ReportXmlResource.EFS_FULL_ECP);
            },
            softAssert -> {
                validationSteps.verifyReportTableNotEmpty();
                validationSteps.verifyUrlContains("/reports");
            },
            "Загрузка ЕФС-1 через XML (Кривоносов)"
        );
    }

    @Test
    @Story("XML Upload - Бабкина В.В.")
    @Description("Тест проверяет загрузку отчёта ЕФС-1 через XML от пользователя Бабкина Вера Васильевна")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_1_xml_babkina() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.BABKINA_VERA);
            },
            () -> {
                reportNavSteps.createNewReportDraft(ReportFormType.EFS1);
                legacySteps.sendXml(ReportXmlResource.EFS_FULL_ECP);
            },
            softAssert -> {
                validationSteps.verifyReportTableNotEmpty();
                validationSteps.verifyUrlContains("/reports");
            },
            "Загрузка ЕФС-1 через XML (Бабкина)"
        );
    }

    // ========== Ручное создание: ТД (Трудовая деятельность) ==========

    @Test
    @AllureId("EFS-002")
    @Story("Manual Creation - ТД")
    @Description("Тест проверяет ручное создание отчёта ЕФС-1 раздел 1.1 ТД (трудовая деятельность)")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_szv_td() {
        arrangeActAssert(
            () -> {
                legacySteps.authorizeEVS(UITypeSelector.getSelectedUIType());
            },
            () -> {
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
            },
            softAssert -> {
                validationSteps.verifyNoValidationErrors();
            },
            "Ручное создание ЕФС-1 ТД"
        );
    }

    @Test
    @AllureId("EFS-006")
    @Story("Manual Creation - ТД - Кривоносов А.П.")
    @Description("Тест проверяет ручное создание отчёта ЕФС-1 раздел 1.1 ТД от пользователя Кривоносов Александр Петрович")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_szv_td_krivonosov() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
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
            },
            softAssert -> {
                validationSteps.verifyNoValidationErrors();
            },
            "Ручное создание ЕФС-1 ТД (Кривоносов)"
        );
    }

    // ========== Ручное создание: СТАЖ ==========

    @Test
    @Story("Manual Creation - СТАЖ")
    @Description("Тест проверяет ручное создание отчёта ЕФС-1 раздел 1.2 СТАЖ: ЗЛ, период стажа, сохранение.")
    @Severity(SeverityLevel.NORMAL)
    @AllureId("EFS-STAJ-001")
    public void efs_szv_staj() {
        arrangeActAssert(
            () -> {
                legacySteps.authorizeEVS(UITypeSelector.getSelectedUIType());
            },
            () -> {
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
            },
            softAssert -> {
                validationSteps.verifyNoValidationErrors();
            },
            "Ручное создание ЕФС-1 СТАЖ"
        );
    }
}