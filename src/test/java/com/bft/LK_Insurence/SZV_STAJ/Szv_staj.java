package com.bft.LK_Insurence.SZV_STAJ;

import com.bft.test.base.UITestBase;
import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
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
 * Тесты для отчёта СЗВ-СТАЖ (Сведения о страховом стаже)
 * 
 * Покрывает сценарий загрузки отчёта СЗВ-СТАЖ через XML файл.
 */
@Epic("Формы отчетности")
@Feature("СЗВ-СТАЖ Reports")
public class Szv_staj extends UITestBase {
    
    @Test(groups = {"web", "szv-staj", "smoke", "xml-upload"}, 
          testName = "#1 Загрузка отчёта СЗВ-СТАЖ через XML файл",
          description = "Загрузка отчёта СЗВ-СТАЖ через XML файл")
    @AllureId("SZVSTAJ-001")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта СЗВ-СТАЖ через XML файл в систему ЕВС")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_staj_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType());
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportFormType.SZVSTAZH); //Выбор отчета
        steps.addNewReport(); // выбор нового отчета
        steps.sendXml(ReportXmlResource.SZV_STAZH_AF2); //отправка xml
    }
}
