package com.bft.LK_Insurence.EFS_1;

import com.bft.BaseTest;
import com.bft.enums.ReportType;
import com.bft.enums.UITypeSelector;
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

import static com.codeborne.selenide.Selenide.$x;

/**
 * Тесты для отчёта ЕФС-1 (Единая форма сведений)
 * 
 * Покрывает различные сценарии создания и отправки отчётов ЕФС-1:
 * - Загрузка через XML файл
 * - Ручное создание ТД (трудовая деятельность)
 * - Ручное создание СТАЖ
 * 
 * Поддерживает авторизацию от разных пользователей через {@link TestUsers}
 */
@Epic("Формы отчетности")
@Feature("EFS-1 Reports")
public class Efs1 extends BaseTest {
    
    @Test(groups = {"web", "efs", "smoke", "xml-upload"}, 
          testName = "#1 Загрузка отчёта ЕФС-1 через XML файл",
          description = "Загрузка отчёта ЕФС-1 через XML файл")
    @AllureId("EFS-001")
    @Story("XML Upload")
    @Description("Тест проверяет возможность загрузки отчёта ЕФС-1 через XML файл с ЭЦП")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_1_xml() {

        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType()); // контур из evs.ui.type (локально/GitLab)
        steps.addReports();
        steps.selectReportType(ReportType.EFS1);
        steps.addNewReport();
        steps.sendXml(ReportType.EFS_FULL_ECP);
        /*steps.chooseCriptoProvider(); //Выбор провайдера
        steps.selectCertificate(); //Выбор сертификата*/
    }

    @Test(groups = {"web", "efs", "regression", "manual-creation"}, 
          testName = "#2 Ручное создание отчёта ЕФС-1 ТД",
          description = "Ручное создание отчёта ЕФС-1 ТД")
    @AllureId("EFS-002")
    @Story("Manual Creation - ТД")
    @Description("Тест проверяет ручное создание отчёта ЕФС-1 раздел 1.1 ТД (трудовая деятельность)")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_szv_td() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType());
        steps.addReports();
        steps.selectReportType(ReportType.EFS1);
        steps.addNewReport(); // Создание нового черновика
        steps.addGeneralInfoEFS(); // Заполнение общих сведений
        steps.createContinue(); //Продолжить
        steps.sidebar(); // Выбор раздела 1.1 ТД, 1.2 СТАЖ, 1.3 БЮДЖ
        steps.addZL(); //Добавление ЗЛ
        steps.fillZLEFS(); //Заполнение ЗЛ
        steps.addEventEFS();
        steps.saveEvent();
        steps.saveZL();
    }

    /**
     * Ручное создание отчёта ЕФС-1 раздел 1.2 СТАЖ (страховой стаж).
     * <p>Сценарий: авторизация → Отчеты → ЕФС-1 → Создать новый → общие сведения →
     * Раздел 1 → 1.2 СТАЖ → добавление ЗЛ → заполнение ЗЛ → добавление сведений о стаже
     * (тип сведений, отчётный период, начало/конец периода) → сохранение ЗЛ.
     * <p>Проверка: после сохранения ЗЛ не отображается сообщение «Поле обязательно для заполнения».
     */
    @Test(groups = {"web", "efs", "regression", "manual-creation"},
          description = "Ручное создание отчёта ЕФС-1 раздел 1.2 СТАЖ")
    @Story("Manual Creation - СТАЖ")
    @Description("Тест проверяет ручное создание отчёта ЕФС-1 раздел 1.2 СТАЖ (страховой стаж): ЗЛ, период стажа (Начало/Конец периода), сохранение.")
    @Severity(SeverityLevel.NORMAL)
    @AllureId("EFS-STAJ-001")
    public void efs_szv_staj() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType());
        steps.addReports();
        steps.selectReportType(ReportType.EFS1);
        steps.addNewReport();
        steps.addGeneralInfoEFS();
        steps.createContinue();
        steps.sidebar();
        steps.addZL();
        steps.fillZLEFS();
        steps.addSTAJ();
        steps.saveZL();

        // Проверка: после сохранения ЗЛ нет ошибки валидации по обязательным полям
        boolean hasRequiredFieldError = $x("//*[contains(text(),'Поле обязательно для заполнения')]").exists();
        softAssert.assertFalse(hasRequiredFieldError,
                "После сохранения ЗЛ не должно отображаться сообщение «Поле обязательно для заполнения»");
        softAssert.assertAll();
    }
    
    // ========== Тесты с конкретными пользователями ==========
    
    @Test(groups = {"web", "efs", "smoke", "xml-upload", "user-specific"}, 
          testName = "#4 Загрузка отчёта ЕФС-1 через XML от пользователя Кривоносов",
          description = "Загрузка отчёта ЕФС-1 через XML от пользователя Кривоносов")
    @AllureId("EFS-004")
    @Story("XML Upload - Кривоносов А.П.")
    @Description("Тест проверяет загрузку отчёта ЕФС-1 через XML от пользователя Кривоносов Александр Петрович (ОРГАНИЗАЦИЯ -1546025669)")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_1_xml_krivonosov() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
        steps.addReports();
        steps.selectReportType(ReportType.EFS1);
        steps.addNewReport();
        steps.sendXml(ReportType.EFS_FULL_ECP);
    }
    
    @Test(groups = {"web", "efs", "smoke", "xml-upload", "user-specific"}, 
          description = "Загрузка отчёта ЕФС-1 через XML от пользователя Бездомный")
    @Story("XML Upload - Бездомный И.Н.")
    @Description("Тест проверяет загрузку отчёта ЕФС-1 через XML от пользователя Бездомный Иван Николаевич (ОРГАНИЗАЦИЯ -2036470831)")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_1_xml_bezdomniy() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.BEZDOMNIY_IVAN);
        steps.addReports();
        steps.selectReportType(ReportType.EFS1);
        steps.addNewReport();
        steps.sendXml(ReportType.EFS_FULL_ECP);
    }
    
    @Test(groups = {"web", "efs", "regression", "manual-creation", "user-specific"}, 
          testName = "#6 Ручное создание отчёта ЕФС-1 ТД от пользователя Кривоносов",
          description = "Ручное создание отчёта ЕФС-1 ТД от пользователя Кривоносов")
    @AllureId("EFS-006")
    @Story("Manual Creation - ТД - Кривоносов А.П.")
    @Description("Тест проверяет ручное создание отчёта ЕФС-1 раздел 1.1 ТД от пользователя Кривоносов Александр Петрович")
    @Severity(SeverityLevel.CRITICAL)
    public void efs_szv_td_krivonosov() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
        steps.addReports();
        steps.selectReportType(ReportType.EFS1);
        steps.addNewReport();
        steps.addGeneralInfoEFS();
        steps.createContinue();
        steps.sidebar();
        steps.addZL();
        steps.fillZLEFS();
        steps.addEventEFS();
        steps.saveEvent();
        steps.saveZL();
    }
}
