package com.bft.LK_Insurence.SZV_TD;

import com.bft.test.base.UITestBase;
import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
import com.bft.enums.UIType;
import com.bft.security.TestUsers;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

/**
 * Тесты для отчёта СЗВ-ТД (Сведения о трудовой деятельности)
 * 
 * Покрывает сценарий ручного создания отчёта СЗВ-ТД с заполнением трудовых событий.
 * Загрузка СЗВ-ТД через XML покрыта параметризованным тестом
 * {@link com.bft.LK_Insurence.ReportXmlUploadTest}.
 */
@Epic("Формы отчетности")
@Feature("СЗВ-ТД Reports")
public class Szv_td extends UITestBase {

    @Test(groups = {"web", "szv-td", "regression", "manual-creation"}, 
          testName = "#1 Ручное создание отчёта СЗВ-ТД",
          description = "Ручное создание отчёта СЗВ-ТД")
    @AllureId("SZVTD-001")
    @Story("Manual Creation")
    @Description("Тест проверяет полный цикл создания отчета СЗВ-ТД с заполнением всех полей и добавлением застрахованного лица")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_td() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType(UIType.EVS_TEST_LKS));
        steps.addReports();
        steps.selectReportType(ReportFormType.SZVTD);
        steps.addNewReport();
        steps.addGeneralInfoTD();
        steps.createContinue();
        steps.goToZL(); //Переход во вкладку ЗЛ
        steps.addZL(); //Добавление ЗЛ
        steps.fillZL(); //Заполнение ЗЛ
        steps.fillZLBirth();
        steps.addEvent();
        steps.saveEvent();
        steps.saveZL();
    }

    @Test(groups = {"web", "szv-td", "smoke", "xml-upload", "user-specific"},
            testName = "#2 Загрузка отчёта СЗВ-ТД через XML (Кривоносов А.П.)",
            description = "Загрузка отчёта СЗВ-ТД через XML от пользователя Кривоносов")
    @AllureId("SZVTD-002")
    @Story("XML Upload - Кривоносов А.П.")
    @Description("Тест проверяет загрузку отчёта СЗВ-ТД через XML от пользователя Кривоносов Александр Петрович")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_td_xml_krivonosov() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
        steps.addReports();
        steps.selectReportType(ReportFormType.SZVTD);
        steps.addNewReport();
        steps.sendXml(ReportXmlResource.SZV_TD_TYPE1);
    }
}
