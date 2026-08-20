package com.bft.LK_Insurence.SZV_DSO;

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
 * Тесты для отчёта СЗВ-ДСО (Сведения о договорах страхового обеспечения).
 *
 * <p>Покрывает загрузку отчёта СЗВ-ДСО через XML (по аналогии с
 * {@link com.bft.LK_Insurence.EFS_1.Efs1#efs_1_xml_krivonosov}).
 */
@Epic("Формы отчетности")
@Feature("СЗВ-ДСО Reports")
public class Szv_dso extends UITestBase {

    private final AuthSteps authSteps = new AuthSteps();
    private final ReportNavigationSteps reportNavSteps = new ReportNavigationSteps();
    private final SzvReportsSteps legacySteps = new SzvReportsSteps();

    @Test(groups = {"web", "szv-dso", "smoke", "xml-upload", "user-specific"},
            testName = "#1 Загрузка отчёта СЗВ-ДСО через XML (Кривоносов А.П.)",
            description = "Загрузка отчёта СЗВ-ДСО через XML от пользователя Кривоносов")
    @AllureId("SZVDSO-001")
    @Story("XML Upload - Кривоносов А.П.")
    @Description("Тест проверяет загрузку отчёта СЗВ-ДСО через XML от пользователя Кривоносов Александр Петрович")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_dso_xml_krivonosov() {
        authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
        reportNavSteps.createNewReportDraft(ReportFormType.SZVDSO);
        legacySteps.sendXml(ReportXmlResource.SZV_DSO_AF2);
    }
}