package com.bft.LK_Insurence.ODV_1;

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
 * Тесты для отчёта ОДВ-1.
 *
 * <p>Покрывает загрузку отчёта ОДВ-1 через XML (по аналогии с
 * {@link com.bft.LK_Insurence.EFS_1.Efs1#efs_1_xml_krivonosov}).
 */
@Epic("Формы отчетности")
@Feature("ОДВ-1 Reports")
public class Odv1 extends UITestBase {

    private final AuthSteps authSteps = new AuthSteps();
    private final ReportNavigationSteps reportNavSteps = new ReportNavigationSteps();
    private final SzvReportsSteps legacySteps = new SzvReportsSteps();

    @Test(groups = {"web", "odv-1", "smoke", "xml-upload", "user-specific"},
            testName = "#1 Загрузка отчёта ОДВ-1 через XML (Кривоносов А.П.)",
            description = "Загрузка отчёта ОДВ-1 через XML от пользователя Кривоносов")
    @AllureId("ODV-001")
    @Story("XML Upload - Кривоносов А.П.")
    @Description("Тест проверяет загрузку отчёта ОДВ-1 через XML от пользователя Кривоносов Александр Петрович")
    @Severity(SeverityLevel.CRITICAL)
    public void odv_1_xml_krivonosov() {
        authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
        reportNavSteps.createNewReportDraft(ReportFormType.ODV1);
        legacySteps.sendXml(ReportXmlResource.SZV_ODV_1_AF2);
    }
}