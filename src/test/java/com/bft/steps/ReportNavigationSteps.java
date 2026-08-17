package com.bft.steps;

import com.bft.enums.ReportFormType;
import com.bft.ui.pages.MainPage;
import io.qameta.allure.Step;
import org.springframework.stereotype.Component;

/**
 * Шаги для навигации к разделу отчетов и создания черновика.
 */
@Component
public class ReportNavigationSteps {

    @Step("Переход к разделу отчетов")
    public void navigateToReports() {
        new MainPage().openTab("Отчеты");
    }

    @Step("Создание нового черновика отчета типа {reportType}")
    public void createNewReportDraft(ReportFormType reportType) {
        new MainPage()
                .clickButton("Создать отчет") // Проверьте текст кнопки в UI
                .selectReportType(reportType) // Требуется реализация в MainPage или компоненте
                .clickButton("Создать");      // Подтверждение создания
    }

    @Step("Заполнение общих сведений отчета")
    public void fillGeneralInfo(String info) {
        // Заглушка: в реальности здесь будет вызов методов заполнения полей
        new MainPage().clickButton("Продолжить");
    }

    @Step("Переход в реестр запросов (Архив)")
    public void openArchiveRequestsRegistry() {
        new MainPage()
                .openTab("ЛК Архивной Организации")
                .openTab("Реестр запросов")
                .openTab("Управление запросами");
    }
}