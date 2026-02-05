package com.bft.LK_Insurence.SZV_TD;

import com.bft.BaseTest;
import com.bft.enums.ReportType;
import com.bft.enums.UITypeSelector;
import com.bft.enums.UIType;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.*;
import io.qameta.allure.AllureId;
import org.testng.annotations.Test;

/**
 * Тесты для отчёта СЗВ-ТД (Сведения о трудовой деятельности)
 * 
 * Покрывает различные сценарии создания и отправки отчётов СЗВ-ТД:
 * - Ручное создание отчёта с заполнением трудовых событий
 * - Загрузка через XML файл
 */
@Epic("Формы отчетности")
@Feature("СЗВ-ТД Reports")
public class Szv_td extends BaseTest {

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
        steps.selectReportType(ReportType.SZVTD);
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

    @Test(groups = {"web", "szv-td", "smoke", "xml-upload"}, 
          testName = "#2 Загрузка отчёта СЗВ-ТД через XML файл",
          description = "Загрузка отчёта СЗВ-ТД через XML файл")
    @AllureId("SZVTD-002")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта СЗВ-ТД через XML файл в систему ЕВС")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_td_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType());
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportType.SZVTD); //Выбор отчета
        steps.addNewReport(); // выбор нового отчета
        steps.sendXml(ReportType.SZV_TD_TYPE1); //отправка xml
    }
}
