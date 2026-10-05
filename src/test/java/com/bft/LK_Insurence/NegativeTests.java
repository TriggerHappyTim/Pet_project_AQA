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
import com.bft.testdata.TestDataConstants;
import com.bft.ui.pages.MainPage;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.bft.pw.Selenide.$x;

/**
 * Негативные тесты для ЛК Страхователя.
 *
 * <p>Покрывает сценарии проверки валидации данных, обработки ошибок
 * и граничных случаев при работе с отчётами.
 *
 * @see <a href="docs/test-cases-lks-krivonosov.md">Тест-кейсы ЛК Страхователя</a>
 */
@Tag("lk-insurer")
@Tag("negative")
@Epic("Формы отчетности")
@Feature("Негативные сценарии")
public class NegativeTests extends UITestBase {

    private final AuthSteps authSteps = new AuthSteps();
    private final ReportNavigationSteps reportNavSteps = new ReportNavigationSteps();
    private final ReportValidationSteps validationSteps = new ReportValidationSteps();
    private final SzvReportsSteps legacySteps = new SzvReportsSteps();

    // ========== TC-LKS-NEG-001: Валидация обязательных полей ==========

    @Test
    @AllureId("NEG-001")
    @Story("Обязательные поля")
    @Description("Проверка, что при незаполненных обязательных полях отображаются ошибки валидации и переход невозможен")
    @Severity(SeverityLevel.CRITICAL)
    public void validateRequiredFieldsEfs1() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                reportNavSteps.createNewReportDraft(ReportFormType.EFS1);
            },
            softAssert -> {
                // Форма создания ЕФС-1 открыта, но общие сведения не заполнены
                // Проверяем, что кнопка "Продолжить" неактивна или при клике отображается ошибка
                validationSteps.verifyTextPresent("Наименование должности руководителя");

                // Пытаемся нажать "Продолжить" без заполнения обязательных полей
                new MainPage().clickSpanButton("Продолжить");

                // Проверяем появление ошибки валидации
                validationSteps.verifyValidationErrorVisible("обязательно для заполнения");
            },
            "Валидация обязательных полей ЕФС-1"
        );
    }

    // ========== TC-LKS-NEG-002: Загрузка невалидного XML ==========

    @Test
    @AllureId("NEG-002")
    @Story("Невалидный XML")
    @Description("Проверка, что система корректно обрабатывает попытку загрузки невалидного XML файла")
    @Severity(SeverityLevel.CRITICAL)
    public void uploadInvalidXmlEfs1() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                reportNavSteps.createNewReportDraft(ReportFormType.EFS1);
            },
            softAssert -> {
                // Загружаем невалидный XML (текстовый файл как XML)
                new MainPage()
                        .clickBtnSecondary("Загрузить отчет")
                        .uploadFile("src/test/resources/application/invalid-test.xml");

                // Подтверждаем загрузку
                new MainPage().clickButtonModal("Загрузить");
                new MainPage().clickButtonModalDialog("Да");

                // Проверяем наличие ошибки валидации
                validationSteps.verifyTextPresent("ошибк");
            },
            "Загрузка невалидного XML ЕФС-1"
        );
    }

    // ========== TC-LKS-NEG-003: Невалидный СНИЛС ==========

    @Test
    @AllureId("NEG-003")
    @Story("Невалидный СНИЛС")
    @Description("Проверка валидации формата СНИЛС при заполнении данных застрахованного лица")
    @Severity(SeverityLevel.CRITICAL)
    public void validateInvalidSnils() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                legacySteps.addReports();
                legacySteps.selectReportType(ReportFormType.EFS1);
                legacySteps.addNewReport();
                legacySteps.addGeneralInfoEFS();
                legacySteps.createContinue();
                legacySteps.sidebar();
                legacySteps.addZL();
            },
            softAssert -> {
                // Заполняем ФИО и некорректный СНИЛС
                new MainPage()
                        .inputFieldPerson("personSurname", TestDataConstants.InsuredPerson.LAST_NAME_1)
                        .inputFieldPerson("personName", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                        .inputFieldPerson("personMiddlename", TestDataConstants.InsuredPerson.MIDDLE_NAME_1)
                        .clickMuiInputBase("personSnils", "123");

                // Пытаемся сохранить
                new MainPage().clickBtnPrimary("Сохранить");

                // Проверяем ошибку валидации СНИЛС
                validationSteps.verifyValidationErrorVisible("СНИЛС");
            },
            "Валидация невалидного СНИЛС"
        );
    }

    // ========== TC-LKS-NEG-004: Отправка без ЭЦП ==========

    @Test
    @AllureId("NEG-004")
    @Story("Отправка без ЭЦП")
    @Description("Проверка, что при закрытии окна выбора сертификата статус отчёта остаётся 'Черновик'")
    @Severity(SeverityLevel.CRITICAL)
    public void sendWithoutCertificate() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                // Ручное создание ЕФС-1 с данными ЗЛ (как в проходящих тестах)
                legacySteps.addReports();
                legacySteps.selectReportType(ReportFormType.EFS1);
                legacySteps.addNewReport();
                legacySteps.addGeneralInfoEFS();
                legacySteps.createContinue();
                legacySteps.sidebar();
                legacySteps.addZL();
                legacySteps.fillZLEFS();
                legacySteps.addEventEFS();
                legacySteps.saveEvent();
                legacySteps.saveZL();
            },
            softAssert -> {
                // После saveZL() мы на странице списка отчётов.
                // Открываем наш отчёт
                new MainPage().openReportUos();

                // Нажимаем "Подписать и отправить"
                new MainPage().clickBtnPrimary("Подписать и отправить");
                new MainPage().clickButtonModalDialog("Да");

                // Ожидаем появления кнопки "Подписать"
                $x("//button[contains(@class, 'btn-primary')]//span[text() = 'Подписать']")
                        .shouldBe(com.bft.pw.Condition.visible,
                                java.time.Duration.ofSeconds(120));

                // Нажимаем "Подписать"
                new MainPage().clickBtnPrimary("Подписать");

                // Выбираем КриптоПРО
                new MainPage().clickSpanCloseDialog("КриптоПРО");
                new MainPage().clickButtonCloseDialog("Выбрать");

                // Закрываем диалог выбора сертификата (отмена)
                new MainPage().pressEscape();

                // Проверяем, что отчёт остался в статусе черновика (не отправлен)
                validationSteps.verifyTextNotPresent("Отправлен");
            },
            "Отправка отчёта без ЭЦП"
        );
    }

    // ========== TC-LKS-NEG-005: Дубликат ЗЛ по СНИЛС ==========

    @Test
    @AllureId("NEG-005")
    @Story("Дубликат ЗЛ")
    @Description("Проверка, что система корректно обрабатывает попытку добавить дублирующееся застрахованное лицо")
    @Severity(SeverityLevel.NORMAL)
    public void validateDuplicateZL() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                legacySteps.addReports();
                legacySteps.selectReportType(ReportFormType.EFS1);
                legacySteps.addNewReport();
                legacySteps.addGeneralInfoEFS();
                legacySteps.createContinue();
                legacySteps.sidebar();
                legacySteps.addZL();
                legacySteps.fillZLEFS();
                legacySteps.saveZL();
            },
            softAssert -> {
                // Пытаемся добавить второе ЗЛ с теми же данными (включая СНИЛС)
                new MainPage().clickBtnSecondary("Добавить");

                new MainPage()
                        .inputFieldPerson("personSurname", TestDataConstants.InsuredPerson.LAST_NAME_1)
                        .inputFieldPerson("personName", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                        .inputFieldPerson("personMiddlename", TestDataConstants.InsuredPerson.MIDDLE_NAME_1)
                        .clickMuiInputBase("personSnils", TestDataConstants.InsuredPerson.SNILS_1);

                // Пытаемся сохранить
                new MainPage().clickBtnPrimary("Сохранить");

                // Проверяем наличие ошибки о дублировании
                validationSteps.verifyValidationErrorVisible("СНИЛС");
            },
            "Дубликат ЗЛ по СНИЛС"
        );
    }

    // ========== TC-LKS-SZVISH-004: Невалидный XML СЗВ-ИСХ ==========

    @Test
    @AllureId("SZVISH-NEG-004")
    @Story("Невалидный XML СЗВ-ИСХ")
    @Description("Проверка, что при загрузке невалидного XML в форму СЗВ-ИСХ отображается ошибка валидации")
    @Severity(SeverityLevel.CRITICAL)
    public void uploadInvalidXmlSzvIsh() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                reportNavSteps.createNewReportDraft(ReportFormType.SZVISH);
            },
            softAssert -> {
                new MainPage()
                        .clickBtnSecondary("Загрузить отчет")
                        .uploadFile("src/test/resources/application/invalid-test.xml");

                new MainPage().clickButtonModal("Загрузить");
                new MainPage().clickButtonModalDialog("Да");

                validationSteps.verifyTextPresent("ошибк");
            },
            "Загрузка невалидного XML в СЗВ-ИСХ"
        );
    }

    // ========== TC-LKS-INTEGRATION-003: Протоколы УПП с ошибками ==========

    @Test
    @AllureId("INT-003")
    @Story("Протоколы УПП ошибки")
    @Description("Проверка, что при обработке отчёта с ошибками протокол содержит описания ошибок")
    @Severity(SeverityLevel.NORMAL)
    public void checkProtocolsWithErrors() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                // Ручное создание ЕФС-1 без заполнения разделов — отправка пустого отчёта
                legacySteps.addReports();
                legacySteps.selectReportType(ReportFormType.EFS1);
                legacySteps.addNewReport();
                legacySteps.addGeneralInfoEFS();
                legacySteps.createContinue();

                // Отправляем отчёт (пустой — должен вызвать ошибки в протоколе)
                legacySteps.submitAndSend();
            },
            softAssert -> {
                // Navigate to protocols list
                com.bft.pw.Selenide.open("https://ecp-test.sfr.gov.ru/insurer/#/protocols");
                new MainPage().waitTableToLoad();

                validationSteps.verifyReportTableNotEmpty();
            },
            "Протоколы УПП с ошибками"
        );
    }

    // ========== TC-LKS-AUTH-002: Выход из системы ==========

    @Test
    @AllureId("AUTH-002")
    @Story("Выход из системы")
    @Description("Проверка, что после нажатия 'Завершить сессию' пользователь выходит из системы (проверка BUG-LKS-008)")
    @Severity(SeverityLevel.CRITICAL)
    public void logoutFromSystem() {
        arrangeActAssert(
            () -> {
                authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
            },
            () -> {
                // Выполняем выход
                new MainPage().logOut();
            },
            softAssert -> {
                // After logout from insurer, user is redirected to main portal page
                String url = com.bft.pw.PwSession.url();
                softAssert.assertTrue(
                    !url.contains("/insurer"),
                    "Пользователь должен быть перенаправлен из модуля Страхователя после выхода"
                );
                softAssert.assertTrue(
                    url.contains("ecp-test.sfr.gov.ru"),
                    "Пользователь должен оставаться на портале после выхода из Страхователя"
                );
            },
            "Выход из системы"
        );
    }
}
