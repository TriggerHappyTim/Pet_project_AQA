package com.bft.LK_Insurence.SZV_DSO;

import com.bft.test.base.UITestBase;
import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.*;
import io.qameta.allure.AllureId;
import org.testng.annotations.Test;

/**
 * Тесты для отчёта СЗВ-ДСО (Сведения о дополнительном социальном обеспечении)
 * 
 * Покрывает сценарий загрузки отчёта СЗВ-ДСО через XML файл.
 */
@Epic("Формы отчетности")
@Feature("СЗВ-ДСО Reports")
public class Szv_dso extends UITestBase {
    
    @Test(groups = {"web", "szv-dso", "smoke", "xml-upload"}, 
          testName = "#1 Загрузка отчёта СЗВ-ДСО через XML файл",
          description = "Загрузка отчёта СЗВ-ДСО через XML файл")
    @AllureId("SZVDSO-001")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта СЗВ-ДСО через XML файл в систему ЕВС")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_dso_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType());
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportFormType.SZVDSO); //Выбор отчета
        steps.addNewReport(); // выбор нового отчета
        steps.sendXml(ReportXmlResource.SZV_DSO_AF2); //отправка xml
    }
}
