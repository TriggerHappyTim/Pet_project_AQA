package com.bft.LK_Insurence.SZV_ISH;

import com.bft.test.base.UITestBase;
import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
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
 * Тесты для отчёта СЗВ-ИСХ (Сведения исходные)
 * 
 * Покрывает сценарии загрузки через XML и ручного заполнения отчёта СЗВ-ИСХ.
 * 
 * <p>Вход в систему: Госуслуги (ЕПГУ), пользователь Кривоносов А.П.,
 * организация, начинающаяся на «ОРГАНИЗАЦИЯ -154» (evs.user1.organization).
 * 
 * @see TestUsers#KRIVONOSOV_ALEXANDER
 */
@Tag("web")
@Tag("xml-upload")
@Epic("Формы отчетности")
@Feature("СЗВ-ИСХ Reports")
public class Szv_ish extends UITestBase {

    private final ReportValidationSteps validationSteps = new ReportValidationSteps();

    @Test
    @Story("Manual Creation")
    @Description("Тест проверяет ручное создание отчёта СЗВ-ИСХ по аналогии с ЕФС-1")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_ish_manual() {
        arrangeActAssert(
            () -> {
                SzvReportsSteps steps = new SzvReportsSteps();
                steps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                SzvReportsSteps steps = new SzvReportsSteps();
                steps.addReports();
                steps.selectReportType(ReportFormType.SZVISH);
                steps.addNewReport();
                steps.addGeneralInfoISH();
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
            "Ручное создание СЗВ-ИСХ"
        );
    }
    
    @Test
    @AllureId("SZVISH-002")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта СЗВ-ИСХ через XML файл в систему ЕВС")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_ish_xml() {
        arrangeActAssert(
            () -> {
                SzvReportsSteps steps = new SzvReportsSteps();
                steps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                SzvReportsSteps steps = new SzvReportsSteps();
                steps.addReports();
                steps.selectReportType(ReportFormType.SZVISH);
                steps.addNewReport();
                steps.sendXml(ReportXmlResource.SZV_ISH_AF2);
            },
            softAssert -> {
                validationSteps.verifyReportTableNotEmpty();
                validationSteps.verifyUrlContains("/reports");
            },
            "Загрузка СЗВ-ИСХ через XML"
        );
    }

    @Test
    @AllureId("SZVISH-005")
    @Story("Form Availability")
    @Description("Проверка доступности типа СЗВ-ИСХ и формы загрузки/создания")
    @Severity(SeverityLevel.NORMAL)
    public void szv_ish_form_availability() {
        arrangeActAssert(
            () -> {
                SzvReportsSteps steps = new SzvReportsSteps();
                steps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                SzvReportsSteps steps = new SzvReportsSteps();
                steps.addReports();
                steps.selectReportType(ReportFormType.SZVISH);
                steps.addNewReport();
            },
            softAssert -> {
                validationSteps.verifyTextPresent("Загрузить отчет");
                validationSteps.verifyUrlContains("/reports");
            },
            "Доступность формы СЗВ-ИСХ"
        );
    }
}
