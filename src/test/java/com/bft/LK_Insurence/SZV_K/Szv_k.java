package com.bft.LK_Insurence.SZV_K;

import com.bft.BaseTest;
import com.bft.enums.ReportType;
import com.bft.enums.UIType;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.*;
import org.testng.annotations.Test;

/**
 * Тесты для отчёта СЗВ-К (Сведения корректирующие по взносам)
 * 
 * Покрывает сценарий загрузки отчёта СЗВ-К через XML файл.
 */
@Epic("Формы отчетности")
@Feature("СЗВ-К Reports")
public class Szv_k extends BaseTest {
    
    @Test(groups = {"web", "szv-k", "smoke", "xml-upload"}, 
          description = "Загрузка отчёта СЗВ-К через XML файл")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта СЗВ-К через XML файл в систему ЕВС")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_k_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UIType.EVS_UAT_LKS); //Авторизация в ЕВС
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportType.SZVK); //Выбор отчета
        steps.addNewReport(); // выбор нового отчета
        steps.sendXml(ReportType.SZV_K_AF2); //отправка xml
    }
}
