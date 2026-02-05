package com.bft.LK_Insurence.ODV_1;

import com.bft.BaseTest;
import com.bft.enums.ReportType;
import com.bft.enums.UITypeSelector;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.*;
import io.qameta.allure.AllureId;
import org.testng.annotations.Test;

/**
 * Тесты для отчёта ОДВ-1 (Опись документов, представляемых страхователем)
 * 
 * Покрывает сценарий загрузки отчёта ОДВ-1 через XML файл.
 */
@Epic("Формы отчетности")
@Feature("ОДВ-1 Reports")
public class Odv1 extends BaseTest {
    
    @Test(groups = {"web", "odv", "smoke", "xml-upload"}, 
          testName = "#1 Загрузка отчёта ОДВ-1 через XML файл",
          description = "Загрузка отчёта ОДВ-1 через XML файл")
    @AllureId("ODV-001")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта ОДВ-1 через XML файл в систему ЕВС")
    @Severity(SeverityLevel.CRITICAL)
    public void odv_1_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType());
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportType.ODV1); //Выбор отчета
        steps.addNewReport(); // выбор нового отчета
        steps.sendXml(ReportType.SZV_ODV_1_AF2); //отправка xml
    }
}
