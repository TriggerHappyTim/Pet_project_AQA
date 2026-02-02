package com.bft.LK_Insurence.SZV_STAJ;

import com.bft.BaseTest;
import com.bft.enums.ReportType;
import com.bft.enums.UIType;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.*;
import org.testng.annotations.Test;

/**
 * Тесты для отчёта СЗВ-СТАЖ (Сведения о страховом стаже)
 * 
 * Покрывает сценарий загрузки отчёта СЗВ-СТАЖ через XML файл.
 */
@Epic("Формы отчетности")
@Feature("СЗВ-СТАЖ Reports")
public class Szv_staj extends BaseTest {
    
    @Test(groups = {"web", "szv-staj", "smoke", "xml-upload"}, 
          description = "Загрузка отчёта СЗВ-СТАЖ через XML файл")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта СЗВ-СТАЖ через XML файл в систему ЕВС")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_staj_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UIType.EVS_UAT_LKS); //Авторизация в ЕВС
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportType.SZVSTAZH); //Выбор отчета
        steps.addNewReport(); // выбор нового отчета
        steps.sendXml(ReportType.SZV_STAZH_AF2); //отправка xml
    }
}
