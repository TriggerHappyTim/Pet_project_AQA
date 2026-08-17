package com.bft.LK_Insurence.SZV_M;

import com.bft.test.base.UITestBase;
import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
import com.bft.enums.UIType;
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
 * Тесты для отчёта СЗВ-М (Сведения о застрахованных лицах)
 * 
 * Покрывает различные сценарии создания и отправки отчётов СЗВ-М:
 * - Ручное создание отчёта с заполнением застрахованных лиц
 * - Загрузка через XML файл
 */
@Epic("Формы отчетности")
@Feature("СЗВ-М Reports")
public class Szv_m extends UITestBase {

    @Test(groups = {"web", "szv-m", "regression", "manual-creation"}, 
          description = "Ручное создание отчёта СЗВ-М")
    @Story("Manual Creation")
    @Description("Тест проверяет полный цикл ручного создания отчёта СЗВ-М с заполнением застрахованных лиц")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_m() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType(UIType.EVS_UAT_LKS));
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportFormType.SZVM); //Выбор отчета
        steps.addNewReport(); //Создание нового отчета
        steps.addGeneralInfo(); //Заполнение основной информации
        steps.createContinue(); //Нажать на кнопку "Продолжить"
        steps.goToZL(); //Переход во вкладку ЗЛ
        steps.addZL(); //Добавление ЗЛ
        steps.fillZL(); //Заполнение ЗЛ
        steps.fillZLINN();
        steps.saveZL();
    }

    @Test(groups = {"web", "szv-m", "smoke", "xml-upload"}, 
          testName = "#2 Загрузка отчёта СЗВ-М через XML файл",
          description = "Загрузка отчёта СЗВ-М через XML файл")
    @AllureId("SZVM-002")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта СЗВ-М через XML файл в систему ЕВС")
    @Severity(SeverityLevel.CRITICAL)
    public void szv_m_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType());
        steps.addReports(); //Добавление отчета
        steps.selectReportType(ReportFormType.SZVM); //Выбор отчета
        steps.addNewReport(); // выбор нового отчета
        steps.sendXml(ReportXmlResource.SZV_M); //отправка xml
    }
}
