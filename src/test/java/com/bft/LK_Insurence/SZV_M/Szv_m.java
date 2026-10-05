package com.bft.LK_Insurence.SZV_M;

import com.bft.test.base.UITestBase;
import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
import com.bft.enums.UIType;
import com.bft.security.TestUsers;
import com.bft.steps.ReportValidationSteps;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Тесты для отчёта СЗВ-М (Сведения о застрахованных лицах)
 * 
 * Покрывает сценарий ручного создания отчёта СЗВ-М с заполнением застрахованных лиц.
 * Загрузка СЗВ-М через XML покрыта параметризованным тестом
 * {@link com.bft.LK_Insurence.ReportXmlUploadTest}.
 */
@Tag("lk-insurer")
@Tag("web")
@Epic("Формы отчетности")
@Feature("СЗВ-М Reports")
public class Szv_m extends UITestBase {

    private final ReportValidationSteps validationSteps = new ReportValidationSteps();

    @Test
    @Story("Manual Creation")
    @Description("Тест проверяет полный цикл ручного создания отчёта СЗВ-М с заполнением застрахованных лиц")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_m() {
        arrangeActAssert(
            () -> {
                SzvReportsSteps steps = new SzvReportsSteps();
                steps.authorizeEVS(UITypeSelector.getSelectedUIType(UIType.EVS_UAT_LKS));
            },
            () -> {
                SzvReportsSteps steps = new SzvReportsSteps();
                steps.addReports();
                steps.selectReportType(ReportFormType.SZVM);
                steps.addNewReport();
                steps.addGeneralInfo();
                steps.createContinue();
                steps.goToZL();
                steps.addZL();
                steps.fillZL();
                steps.fillZLINN();
                steps.saveZL();
            },
            softAssert -> {
                validationSteps.verifyNoValidationErrors();
            },
            "Ручное создание СЗВ-М"
        );
    }

    @Test
    @AllureId("SZVM-001")
    @Story("XML Upload - Кривоносов А.П.")
    @Description("Тест проверяет загрузку отчёта СЗВ-М через XML от пользователя Кривоносов Александр Петрович")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_m_xml_krivonosov() {
        arrangeActAssert(
            () -> {
                SzvReportsSteps steps = new SzvReportsSteps();
                steps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                SzvReportsSteps steps = new SzvReportsSteps();
                steps.addReports();
                steps.selectReportType(ReportFormType.SZVM);
                steps.addNewReport();
                steps.sendXml(ReportXmlResource.SZV_M);
            },
            softAssert -> {
                validationSteps.verifyReportTableNotEmpty();
                validationSteps.verifyUrlContains("/reports");
            },
            "Загрузка СЗВ-М через XML (Кривоносов)"
        );
    }
}
