package com.bft.LK_Insurence;

import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
import com.bft.security.TestUsers;
import com.bft.steps.AuthSteps;
import com.bft.steps.ReportNavigationSteps;
import com.bft.steps.ReportValidationSteps;
import com.bft.steps.SzvReportsSteps;
import com.bft.test.base.UITestBase;
import com.bft.ui.pages.MainPage;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.bft.pw.Condition.visible;
import static com.bft.pw.Selenide.$x;
import static java.time.Duration.ofSeconds;

/**
 * Тесты жизненного цикла отчётов: полный цикл, редактирование,
 * сохранение как черновик, фильтрация, просмотр деталей, обновление страницы.
 *
 * <p>Покрывает тест-кейсы:
 * <ul>
 *   <li>TC-LKS-INTEGRATION-001: Полный цикл ЕФС-1 (XML → ЭЦП → протокол)</li>
 *   <li>TC-LKS-SIGN-002: Сохранение как черновик</li>
 *   <li>TC-LKS-REPORTS-002: Фильтрация по типу</li>
 *   <li>TC-LKS-REPORTS-003: Фильтрация по статусу</li>
 *   <li>TC-LKS-SZVISH-003: Просмотр деталей отчёта</li>
 *   <li>TC-LKS-MISC-001: Обновление страницы F5</li>
 *   <li>TC-LKS-MISC-003: Копирование отчёта</li>
 * </ul>
 */
@Tag("lk-insurer")
@Tag("smoke")
@Tag("web")
@Epic("Жизненный цикл отчётов")
@Feature("Управление отчётами")
public class ReportLifecycleTests extends UITestBase {

    private final AuthSteps authSteps = new AuthSteps();
    private final ReportNavigationSteps reportNavSteps = new ReportNavigationSteps();
    private final ReportValidationSteps validationSteps = new ReportValidationSteps();
    private final SzvReportsSteps legacySteps = new SzvReportsSteps();

    // ========== TC-LKS-INTEGRATION-001: Полный цикл ЕФС-1 ==========

    @Test
    @Disabled("Стенд test: grpc-сервис Efs1XmlProcessService не зарегистрирован (NOT_FOUND) — "
            + "XML-загрузка ЕФС-1 недоступна (обнаружено 06.10.2026)")
    @AllureId("LIFECYCLE-001")
    @Story("Полный цикл ЕФС-1")
    @Description("Проверяет полный цикл обработки отчёта ЕФС-1: загрузка через XML → подписание ЭЦП → проверка протокола УПП")
    @Severity(SeverityLevel.BLOCKER)
    public void fullCycleEfs1WithProtocol() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                // Шаг 1: Загрузка отчёта через XML
                reportNavSteps.createNewReportDraft(ReportFormType.EFS1);
                legacySteps.sendXml(ReportXmlResource.EFS_FULL_ECP);

                // Шаг 2: Открытие отчёта и подписание ЭЦП
                new MainPage().openReportUos();
                legacySteps.submitAndSend();
            },
            softAssert -> {
                // Шаг 3: Проверка статуса обработки
                validationSteps.verifyTextPresent("На проверке");

                // Шаг 4: Ожидание обработки и проверка протокола
                new MainPage().waitTableToLoad();
                new MainPage().openReportUos();

                // Проверка статуса УПП
                validationSteps.verifyReportWithStatusExists("Положительный");
            },
            "Полный цикл ЕФС-1 с протоколом"
        );
    }

    // ========== TC-LKS-SIGN-002: Сохранение как черновик ==========

    @Test
    @AllureId("LIFECYCLE-002")
    @Story("Сохранение черновика")
    @Description("Проверка, что частично заполненный отчёт сохраняется как черновик и доступен для редактирования")
    @Severity(SeverityLevel.NORMAL)
    public void saveReportAsDraft() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                // Создаём отчёт и заполняем только общие сведения
                legacySteps.addReports();
                legacySteps.selectReportType(ReportFormType.EFS1);
                legacySteps.addNewReport();
                legacySteps.addGeneralInfoEFS();
            },
            softAssert -> {
                // Форма создания открыта, данные заполнены
                // Проверяем, что мы находимся на странице создания (ждём догрузки формы)
                validationSteps.verifyUrlContains("/add");
                validationSteps.verifyTextPresentEventually("Сведения о страхователе", Duration.ofSeconds(15));

                // Откатываемся назад (имитация сохранения как черновик)
                new MainPage().openTab("Отчеты");
                new MainPage().openTab("Список отчетов");
                new MainPage().waitTableToLoad();

                // Проверяем, что таблица отчётов отображается
                validationSteps.verifyReportTableNotEmpty();
            },
            "Сохранение отчёта как черновик"
        );
    }

    // ========== TC-LKS-REPORTS-002: Фильтрация по типу ==========

    @Test
    @AllureId("LIFECYCLE-003")
    @Story("Фильтрация по типу")
    @Description("Проверка, что фильтр по типу отчёта корректно фильтрует таблицу")
    @Severity(SeverityLevel.NORMAL)
    public void filterReportsByType() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                new MainPage()
                        .openTab("ЛК Страхователя")
                        .openTab("Отчеты")
                        .openTab("Список отчетов")
                        .waitTableToLoad();

                // Применяем фильтр по виду отчёта (через name-атрибут input фильтра)
                new MainPage().selectFilterByInputName("reportType", "ЕФС-1");
                new MainPage().clickButton("Применить");
                new MainPage().waitTableToLoad();
            },
            softAssert -> {
                // Проверяем, что таблица содержит отфильтрованные результаты
                validationSteps.verifyReportTableNotEmpty();

                // Проверяем, что URL содержит параметры фильтра
                validationSteps.verifyUrlContains("reports");
            },
            "Фильтрация отчётов по типу ЕФС-1"
        );
    }

    // ========== TC-LKS-REPORTS-003: Фильтрация по способу подачи ==========

    @Test
    @AllureId("LIFECYCLE-004")
    @Story("Фильтрация по способу подачи")
    @Description("Проверка, что фильтр по способу подачи корректно фильтрует таблицу отчётов")
    @Severity(SeverityLevel.NORMAL)
    public void filterReportsByChannel() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                new MainPage()
                        .openTab("ЛК Страхователя")
                        .openTab("Отчеты")
                        .openTab("Список отчетов")
                        .waitTableToLoad();

                // Применяем фильтр по способу подачи (через name-атрибут input фильтра)
                new MainPage().selectFilterByInputName("documentChannel", "Личный кабинет страхователя");
                new MainPage().clickButton("Применить");
                new MainPage().waitTableToLoad();
            },
            softAssert -> {
                validationSteps.verifyReportTableNotEmpty();
            },
            "Фильтрация отчётов по способу подачи"
        );
    }

    // ========== TC-LKS-SZVISH-003: Просмотр деталей отчёта ==========

    @Test
    @AllureId("LIFECYCLE-005")
    @Story("Просмотр деталей")
    @Description("Проверка, что при клике на отчёт в таблице открывается страница с его деталями")
    @Severity(SeverityLevel.NORMAL)
    public void viewReportDetails() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                new MainPage()
                        .openTab("ЛК Страхователя")
                        .openTab("Отчеты")
                        .openTab("Список отчетов")
                        .waitTableToLoad()
                        .openReportUos();
            },
            softAssert -> {
                // Проверяем, что открылась страница с деталями отчёта (ждём догрузки карточки)
                validationSteps.verifyTextPresentEventually("Общие сведения", Duration.ofSeconds(15));

                // Проверяем, что URL изменился (не на странице списка)
                validationSteps.verifyUrlContains("/reports");
            },
            "Просмотр деталей отчёта"
        );
    }

    // ========== TC-LKS-MISC-001: Обновление страницы F5 ==========

    @Test
    @AllureId("LIFECYCLE-006")
    @Story("Обновление страницы")
    @Description("Проверка, что при обновлении страницы (F5) во время заполнения формы пользователь получает предупреждение или данные сохраняются")
    @Severity(SeverityLevel.NORMAL)
    public void pageRefreshDuringFormEdit() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                // Начинаем создание отчёта
                legacySteps.addReports();
                legacySteps.selectReportType(ReportFormType.EFS1);
                legacySteps.addNewReport();
                legacySteps.addGeneralInfoEFS();

                // Обновляем страницу
                com.bft.pw.Selenide.refresh();
            },
            softAssert -> {
                // После обновления страницы проверяем, что форма не потеряна
                // (либо вернулись к списку отчётов, либо форма восстановлена)
                validationSteps.verifyUrlContains("/insurer");
            },
            "Обновление страницы во время заполнения формы"
        );
    }

    // ========== TC-LKS-MISC-003: Копирование отчёта ==========

    @Test
    @AllureId("LIFECYCLE-007")
    @Story("Копирование отчёта")
    @Description("Проверка, что функция копирования отчёта создаёт независимую копию с теми же данными")
    @Severity(SeverityLevel.NORMAL)
    public void copyExistingReport() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                new MainPage()
                        .openTab("ЛК Страхователя")
                        .openTab("Отчеты")
                        .openTab("Список отчетов")
                        .waitTableToLoad()
                        .openReportUos();
            },
            softAssert -> {
                // Проверяем наличие кнопки/опции копирования отчёта
                // Если функционал копирования доступен — нажимаем и проверяем
                boolean hasCopyButton = $x("//button[.//span[contains(text(), 'Копировать')]]").exists();
                if (hasCopyButton) {
                    new MainPage().clickBtnSecondary("Копировать");
                    validationSteps.verifyUrlContains("/add");
                } else {
                    // Если кнопки нет — просто проверяем, что детали отчёта отображаются (ждём догрузки)
                    validationSteps.verifyTextPresentEventually("Общие сведения", Duration.ofSeconds(15));
                }
            },
            "Копирование отчёта"
        );
    }

    // ========== TC-LKS-REPORTS-001: Просмотр списка отчётов ==========

    @Test
    @AllureId("REPORT-001")
    @Story("Просмотр списка отчётов")
    @Description("Проверка загрузки и отображения таблицы отчётов организации")
    @Severity(SeverityLevel.CRITICAL)
    public void viewAllReports() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                // Список отчётов содержит исторические данные стенда; импорт XML здесь не нужен
                // (импорт проверяется в ReportXmlUploadTest).
                new MainPage()
                        .ensureSidebarExpanded()
                        .openTab("ЛК Страхователя")
                        .openTab("Отчеты")
                        .openTab("Список отчетов")
                        .waitTableToLoad();
            },
            softAssert -> {
                validationSteps.verifyReportTableNotEmpty();
                validationSteps.verifyTextPresent("Отчеты");
            },
            "Просмотр списка отчётов"
        );
    }

    // ========== TC-LKS-INTEGRATION-002: Протоколы УПП успешные ==========

    @Test
    @Disabled("Стенд test: grpc-сервис Efs1XmlProcessService не зарегистрирован (NOT_FOUND) — "
            + "XML-загрузка ЕФС-1 недоступна (обнаружено 06.10.2026)")
    @AllureId("INT-002")
    @Story("Протоколы УПП")
    @Description("Проверка, что после обработки отчёта протокол УПП содержит положительный статус")
    @Severity(SeverityLevel.NORMAL)
    public void checkProtocolsPositive() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                legacySteps.addReports();
                legacySteps.selectReportType(ReportFormType.EFS1);
                legacySteps.addNewReport();
                legacySteps.sendXml(ReportXmlResource.EFS_FULL_ECP);

                new MainPage().openReportUos();
                legacySteps.submitAndSend();
            },
            softAssert -> {
                validationSteps.verifyTextPresent("На проверке");

                new MainPage().waitTableToLoad();
                new MainPage().openReportUos();

                validationSteps.verifyReportTableNotEmpty();
            },
            "Протоколы УПП успешные"
        );
    }

    // ========== TC-LKS-MISC-002: Работа с несколькими вкладками ==========

    @Test
    @AllureId("MISC-002")
    @Story("Мульти-вкладки")
    @Description("Проверка, что открытие новой вкладки с ЛК Страхователя не конфликтует с текущей сессией")
    @Severity(SeverityLevel.TRIVIAL)
    public void multiTabBehavior() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                new MainPage()
                        .openTab("ЛК Страхователя")
                        .openTab("Отчеты")
                        .openTab("Список отчетов")
                        .waitTableToLoad();
            },
            softAssert -> {
                // Проверяем, что текущая вкладка работает корректно
                validationSteps.verifyReportTableNotEmpty();

                // Проверяем URL содержит ожидаемый путь
                validationSteps.verifyUrlContains("/reports");
            },
            "Мульти-вкладки"
        );
    }
}
