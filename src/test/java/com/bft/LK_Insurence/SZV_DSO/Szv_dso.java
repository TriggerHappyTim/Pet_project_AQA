package com.bft.LK_Insurence.SZV_DSO;

import com.bft.BaseTest;
import com.bft.enums.ReportType;
import com.bft.enums.UIType;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.*;
import org.testng.annotations.Test;

/**
 * Тесты для отчёта СЗВ-ДСО (Сведения о дополнительном социальном обеспечении)
 * 
 * Покрывает сценарий загрузки отчёта СЗВ-ДСО через XML файл.
 */
@Epic("Формы отчетности")
@Feature("СЗВ-ДСО Reports")
public class Szv_dso extends BaseTest {
    
    @Test(groups = {"web", "szv-dso", "smoke", "xml-upload"}, 
          description = "Загрузка отчёта СЗВ-ДСО через XML файл")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта СЗВ-ДСО через XML файл в систему ЕВС")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_dso_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UIType.EVS_UAT_LKS); //Авторизация в ЕВС
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportType.SZVDSO); //Выбор отчета
        steps.addNewReport(); // выбор нового отчета
        steps.sendXml(ReportType.SZV_DSO_AF2); //отправка xml
    }
}
