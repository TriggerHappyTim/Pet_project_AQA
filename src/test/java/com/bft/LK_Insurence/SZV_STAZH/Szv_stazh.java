package com.bft.LK_Insurence.SZV_STAZH;

import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
import com.bft.security.TestUsers;
import com.bft.steps.AuthSteps;
import com.bft.steps.ReportNavigationSteps;
import com.bft.steps.SzvReportsSteps;
import com.bft.test.base.UITestBase;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

/**
 * Тесты для отчёта СЗВ-СТАЖ (сведения о страховом стаже).
 *
 * <p>Покрывает загрузку отчёта СЗВ-СТАЖ через XML (по аналогии с
 * {@link com.bft.LK_Insurence.EFS_1.Efs1#efs_1_xml_krivonosov}).
 */
@Epic("Формы отчетности")
@Feature("СЗВ-СТАЖ Reports")
public class Szv_stazh extends UITestBase {

    private final AuthSteps authSteps = new AuthSteps();
    private final ReportNavigationSteps reportNavSteps = new ReportNavigationSteps();
    private final SzvReportsSteps legacySteps = new SzvReportsSteps();

    @Test(groups = {"web", "szv-stazh", "smoke", "xml-upload", "user-specific"},
            testName = "#1 Загрузка отчёта СЗВ-СТАЖ через XML (Кривоносов А.П.)",
            description = "Загрузка отчёта СЗВ-СТАЖ через XML от пользователя Кривоносов")
    @AllureId("SZVSTAZH-001")
    @Story("XML Upload - Кривоносов А.П.")
    @Description("Тест проверяет загрузку отчёта СЗВ-СТАЖ через XML от пользователя Кривоносов Александр Петрович")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_stazh_xml_krivonosov() {
        authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
        reportNavSteps.createNewReportDraft(ReportFormType.SZVSTAZH);
        legacySteps.sendXml(ReportXmlResource.SZV_STAZH_AF2);
    }
}