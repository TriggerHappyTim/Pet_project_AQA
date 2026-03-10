package com.bft.LK_Insurence.SZV_KORR;

import com.bft.test.base.UITestBase;
import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.*;
import io.qameta.allure.AllureId;
import org.testng.annotations.Test;

/**
 * Тесты для отчёта СЗВ-КОРР (Сведения корректирующие)
 * 
 * Покрывает сценарий загрузки отчёта СЗВ-КОРР через XML файл.
 */
@Epic("Формы отчетности")
@Feature("СЗВ-КОРР Reports")
public class Szv_korr extends UITestBase {
    
    @Test(groups = {"web", "szv-korr", "smoke", "xml-upload"}, 
          testName = "#1 Загрузка отчёта СЗВ-КОРР через XML файл",
          description = "Загрузка отчёта СЗВ-КОРР через XML файл")
    @AllureId("SZVKORR-001")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта СЗВ-КОРР через XML файл в систему ЕВС")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_korr_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType());
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportFormType.SZVKORR); //Выбор отчета
        steps.addNewReport(); // выбор нового отчета
        steps.sendXml(ReportXmlResource.SZV_KORR_AF2); //отправка xml
    }
}
