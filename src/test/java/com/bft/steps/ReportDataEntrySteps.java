package com.bft.steps;

import com.bft.enums.ReportXmlResource;
import com.bft.security.masking.SecureLogger;
import com.bft.testdata.TestDataConstants;
import com.bft.ui.component.GracePeriodDialogComponent;
import com.bft.ui.pages.MainPage;
import com.bft.pw.Condition;
import com.bft.pw.Selenide;
import com.bft.pw.TimeoutException;
import com.bft.pw.WebDriverRunner;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.bft.enums.TimeoutConstants.DEFAULT_WAIT;
import static com.bft.enums.TimeoutConstants.REPORT_PROCESSING_WAIT;
import static com.bft.pw.Selenide.$x;
import static com.bft.pw.Selenide.$$x;

public class ReportDataEntrySteps {

    private final SecureLogger logger = SecureLogger.getLogger(getClass());

    @Step(value = "Общие сведения СЗВ-М")
    public void addGeneralInfo() {
        new MainPage()
                .clickMuiInputSpan("Отчетный период", "Январь")
                .clickMuiInputSpan("За год", "2022")
                .clickRadioInput("Исходная");
    }

    @Step(value = "Общие сведения СЗВ-ИСХ")
    public void addGeneralInfoISH() {
        new MainPage()
                .clickMuiInputSpan("Календарный год", "2007")
                .inputFieldPerson("directorLastName", TestDataConstants.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("directorFirstName", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("directorMiddleName", TestDataConstants.InsuredPerson.MIDDLE_NAME_1)
                .clickSpanId("directorPosition", TestDataConstants.InsuredPerson.JOB)
                .clickMuiInputBase("personCount", "1");
        fillBasisSectionISH();
    }

    @Step(value = "Секция Основание СЗВ-ИСХ")
    public void fillBasisSectionISH() {
        new MainPage()
                .clickMuiInputBase("totalWorkPlaceAmount", "1")
                .clickMuiInputBase("totalActualWorkPlaceAmount", "1");
    }

    @Step(value = "Общие сведения ТД")
    public void addGeneralInfoTD() {
        new MainPage()
                .clickSpanId("directorPosition", TestDataConstants.InsuredPerson.JOB);
    }

    @Step(value = "Общие сведения EFS")
    public void addGeneralInfoEFS() {
        new MainPage()
                .clickSpanId("headPosition", TestDataConstants.InsuredPerson.JOB)
                .inputFieldPerson("headSurname", TestDataConstants.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("headName", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("headMiddlename", TestDataConstants.InsuredPerson.MIDDLE_NAME_1);
    }

    @Step(value = "Добавление ЗЛ")
    public void addZL() {
        new MainPage()
                .clickBtnSecondary("Добавить");
    }

    @Step(value = "Добавить мероприятие")
    public void addEvent() {
        new MainPage()
                .clickBtnSecondary2("Добавить")
                .clickRadioInput("Мероприятие")
                .muiSpan("Вид мероприятия") //Прием
                .inputDateLabel("activity_eventDate", "01.01.2025")
                .clickMInputLabel("activity.basisEvent[0].number", "7542")
                .inputDateByIdViaJs("activity_basisEvent_0_date", "01.01.2025")
                .clickMInputLabel("activity.basisEvent[0].series", "4513")
                .clickMInputLabel("activity.basisEvent[0].name", "Договор")
                .clickMInputLabel("activity.position", "Директор")
                .clickMInputLabel("activity.codeVfByOkz", "1112.3");
    }

    @Step(value = "Добавить мероприятие")
    public void addEventEFS() {
        new MainPage()
                .clickBtnSecondary2("Добавить")
                .muiSpan("Вид мероприятия") //Прием
                .inputDateEventDateViaJs("01.01.2025")
                .clickMInputLabel("position", "Директор")
                .muiSpanValue("Код выполняемой функции по ОКЗ", "option-0") //1111.7
                .clickMInputLabel("documentList[0].name", "Договор")
                .inputDateByIdViaJs("documentList_0_date", "01.01.2025")
                .clickMInputLabel("documentList[0].series", "4513")
                .clickMInputLabel("documentList[0].number", "7542");
    }

    @Step(value = "Заполнение ЗЛ")
    public void fillZL() {
        new MainPage()
                .inputFieldPerson("lastName", TestDataConstants.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("firstName", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("middleName", TestDataConstants.InsuredPerson.MIDDLE_NAME_1)
                .clickMuiInputBase("snils", TestDataConstants.InsuredPerson.SNILS_1);
    }

    @Step(value = "Заполнение ЗЛ")
    public void fillZLEFS() {
        new MainPage()
                .inputFieldPerson("personSurname", TestDataConstants.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("personName", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("personMiddlename", TestDataConstants.InsuredPerson.MIDDLE_NAME_1)
                .clickMuiInputBase("personSnils", TestDataConstants.InsuredPerson.SNILS_1)
                .clickMuiInputBase("birthDate", TestDataConstants.InsuredPerson.Birthday)
                .muiSpan("Статус") //ГРФ
                .muiSpan("Гражданство"); //Афганистан
    }

    @Step(value = "Заполнение ЗЛ ИНН")
    public void fillZLINN() {
        new MainPage()
            .clickMuiInputBase("inn", TestDataConstants.InsuredPerson.INN_1);
    }

    @Step(value = "Заполнение ЗЛ ДР")
    public void fillZLBirth() {
        new MainPage()
                .clickMuiInputBase("birthDateFull", TestDataConstants.InsuredPerson.Birthday);
    }

    @Step(value = "Сохранение ЗЛ")
    public void saveZL() {
        new MainPage()
                .clickBtnPrimary("Сохранить");
    }

    @Step(value = "Сохранение ЗЛ (второй первичной кнопкой)")
    public void saveZLEFS() {
        new MainPage()
                .clickBtnPrimary_2("Сохранить");
    }

    @Step(value = "Сохранение мероприятия")
    public void saveEvent() {
        new MainPage()
                .clickBtnPrimary_2("Сохранить");
    }

    @Step(value = "Добавление СТАЖ")
    public void addSTAJ() {
        MainPage mainPage = new MainPage();

        mainPage
                .clickBtnSecondary3("Добавить")
                .muiSpanValue("Тип сведений", "option-0")   // Исходная
                .muiSpanValue("Отчетный период", "option-0") // 2023
                .clickBtnSecondary5("Добавить");            // Открыть форму периода стажа

        // Явно ждём появления формы льготного стажа (поле даты начала периода).
        // Если форма не открылась — повторяем клик по «Добавить» (значение могло не зарегистрироваться).
        try {
            $x("//input[contains(@id, 'DateAt')]").shouldBe(Condition.visible, DEFAULT_WAIT);
        } catch (Exception firstTry) {
            logger.info("Форма льготного стажа не открылась с первого раза, повторяем клик 'Добавить'");
            mainPage.clickBtnSecondary5("Добавить");
            $x("//input[contains(@id, 'DateAt')]").shouldBe(Condition.visible, DEFAULT_WAIT);
        }

        GracePeriodDialogComponent graceDialog = new GracePeriodDialogComponent();

        graceDialog.fillPeriodDates("01.01.2025", "01.12.2025")
                .clickAddInTable()              // «Добавить» в таблице (открывает форму строки)
                .selectInFormByName("tuBasis", "МКС")           // Код территориальных условий
                .fillCoefficientAndBidShare("0.78", "0.65")     // Коэффициент и Доля ставки

                .selectByFieldOrLabel("isGroundCode", new String[]{"Основание", "Код основания"}, "ПОЛЕ")
                .selectByLabelVariants(new String[]{"Код дополнительных сведений", "Дополнительные сведения", "дополнительных", "сведений", "Код дополнительных"}, "ДЕКРЕТ")
                .selectByLabelVariants(new String[]{"Код особых условий труда", "Особые условия труда"}, "-2")
                .selectByLabelVariants(new String[]{"Код позиции списка", "Позиция списка"}, "11105000")

                .inputByFieldOrLabel("vlForDnpCode", new String[]{"Код для ВЛ ДНП", "ВЛ для ДНП"}, "-СП")
                .inputByFieldOrLabel("vlForDnpBidShare", new String[]{"Доля для ВЛ ДНП", "Доля ставки"}, "0.52")
                .inputByLabelVariants(new String[]{"Номер рабочего места", "Рабочее место"}, "54")
                .selectByLabelVariants(new String[]{"Класс условий труда", "Класс условий"}, "3.1")

                .clickSaveInForm();             // Сохранить строку

        // Сохраняем сведения о периоде работы (родительский диалог «Льготный стаж»)
        new GracePeriodDialogComponent()
                .clickSavePeriodOfWork();

        // Сохраняем сведения о периоде работы / страховом стаже в модалке отчётного периода
        mainPage.clickSaveReportPeriodModal();

        /*new GracePeriodDialogComponent()
                .clickAddInTable();

        mainPage.clickBtnSecondary6("Добавить");*/
    }

    // ===================== СЗВ-СТАЖ =====================

    @Step(value = "Общие сведения СЗВ-СТАЖ")
    public void addGeneralInfoSTAGE() {
        new MainPage()
                .clickRadioInput("Исходная")
                .clickMuiInputSpan("Календарный год", "2022")
                .clickSpanId("directorPosition", TestDataConstants.InsuredPerson.JOB)
                .clickMuiInputBase("personCount", "1");
    }

    @Step(value = "Заполнение раздела СТАЖ (структурные данные)")
    public void fillSectionSTAZH() {
        new MainPage()
                .clickMuiInputBase("structuralUnitName", "Отдел")
                .clickMuiInputBase("professionName", "Инженер")
                .clickMuiInputBase("workPlaceAmount", "1")
                .clickMuiInputBase("actualEmployeeAmount", "1")
                .clickMuiInputBase("workConditionDescription", "Крайний Север")
                .clickMuiInputBase("primaryDocumentsInOut", "Личная карточка")
                .clickFirstMuiAutocompleteOption("outList[0].outCode");
    }

    @Step(value = "Добавление ЗЛ СТАЖА (ФИО/СНИЛС)")
    public void addPersonSTAGE() {
        new MainPage()
                .inputFieldPerson("fioLastName", TestDataConstants.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("fioFirstName", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("fioMiddleName", TestDataConstants.InsuredPerson.MIDDLE_NAME_1)
                .clickMuiInputBase("fioSnils", TestDataConstants.InsuredPerson.SNILS_1);
    }

    // ===================== СЗВ-ИСХ =====================

    @Step(value = "Заполнение ЗЛ ИСХ")
    public void fillZLForISH() {
        try {
            new MainPage()
                    .inputFieldPerson("lastName", TestDataConstants.InsuredPerson.LAST_NAME_1)
                    .inputFieldPerson("firstName", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                    .inputFieldPerson("middleName", TestDataConstants.InsuredPerson.MIDDLE_NAME_1)
                    .clickRadioInput("Трудовой")
                    .clickMuiInputBase("snils", TestDataConstants.InsuredPerson.SNILS_1)
                    .clickMuiInputBase("contractDetailsDate", "01.01.2024")
                    .clickMuiInputBase("contractDetailsNumber", "1")
                    .clickFirstMuiAutocompleteOption("reportingPeriodType")
                    .clickFirstMuiAutocompleteOption("reportingPeriodYear")
                    .clickMuiInputBase("paymentsSum", "100000");
        } catch (Throwable t) {
            WebDriverRunner.saveSourceDump("ish-zl-fill-fail");
            throw t;
        }
    }

    // ===================== ОДВ-1 =====================

    @Step(value = "Общие сведения ОДВ-1")
    public void addGeneralInfoODV1() {
        new MainPage()
                .muiSpanValue("Отчетный период", "option-0")
                .clickMuiInputSpan("Календарный год", "2024")
                .inputFieldPerson("directorLastName", TestDataConstants.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("directorFirstName", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("directorMiddleName", TestDataConstants.InsuredPerson.MIDDLE_NAME_1)
                .clickSpanId("directorPosition", TestDataConstants.InsuredPerson.JOB)
                .clickRadioInput("Корректирующая");
    }

    @Step(value = "Период ОДВ-1 (сведения о подразделении)")
    public void fillOdv1Period() {
        new MainPage()
                .clickMuiInputBase("structuralUnitName", "Отдел")
                .clickMuiInputBase("professionName", "Инженер")
                .clickMuiInputBase("workPlaceAmount", "1")
                .clickMuiInputBase("actualEmployeeAmount", "1")
                .clickMuiInputBase("workConditionDescription", "Обычные")
                .clickMuiInputBase("primaryDocumentsInOut", "Приказ");
    }

    // ===================== СЗВ-КОРР =====================

    @Step(value = "Общие сведения СЗВ-КОРР")
    public void addGeneralInfoKORR() {
        new MainPage()
                .clickRadioInput("Исходная")
                .clickMuiInputSpan("Календарный год", "2024")
                .inputFieldPerson("directorLastName", TestDataConstants.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("directorFirstName", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("directorMiddleName", TestDataConstants.InsuredPerson.MIDDLE_NAME_1)
                .clickSpanId("directorPosition", TestDataConstants.InsuredPerson.JOB)
                .clickMuiInputBase("personCount", "1")
                .clickMuiInputBase("insuranceDebtBeginning", "1")
                .clickMuiInputBase("insuranceDebtEnd", "1")
                .clickMuiInputBase("insuranceAdded", "1")
                .clickMuiInputBase("insurancePayed", "1")
                .clickMuiInputBase("accumulativeDebtBeginning", "1")
                .clickMuiInputBase("accumulativeDebtEnd", "1")
                .clickMuiInputBase("accumulativeAdded", "1")
                .clickMuiInputBase("accumulativePayed", "1")
                .clickMuiInputBase("tarifDebtBeginning", "1")
                .clickMuiInputBase("tarifDebtEnd", "1")
                .clickMuiInputBase("tarifAdded", "1")
                .clickMuiInputBase("tarifPayed", "1");
    }

    // ===================== СЗВ-К =====================

    @Step(value = "Общие сведения СЗВ-К")
    public void addGeneralInfoK() {
        new MainPage()
                .clickRadioInput("Исходная")
                .clickSpanId("headPosition", TestDataConstants.InsuredPerson.JOB)
                .inputFieldPerson("headSurname", TestDataConstants.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("headName", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("headMiddlename", TestDataConstants.InsuredPerson.MIDDLE_NAME_1);
    }

    @Step(value = "Добавление ЗЛ К (ФИО/СНИЛС/ДР)")
    public void addZL_K() {
        new MainPage()
                .inputFieldPerson("personSurname", TestDataConstants.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("personName", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("personMiddlename", TestDataConstants.InsuredPerson.MIDDLE_NAME_1)
                .clickMuiInputBase("snils", TestDataConstants.InsuredPerson.SNILS_1)
                .clickMuiInputBase("birthDate", TestDataConstants.InsuredPerson.Birthday);
    }

    // ===================== СЗВ-DSO =====================

    @Step(value = "Общие сведения СЗВ-DSO")
    public void addGeneralInfoDSO() {
        new MainPage()
                .clickRadioInput("Исходная")
                .clickSpanId("headPosition", TestDataConstants.InsuredPerson.JOB)
                .inputFieldPerson("headSurname", TestDataConstants.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("headName", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("headMidlename", TestDataConstants.InsuredPerson.MIDDLE_NAME_1)
                .clickMuiInputSpan("Календарный год", "2024");
    }

    @Step(value = "Добавление ЗЛ DSO (ФИО/СНИЛС/ДР)")
    public void addZL_DSO() {
        new MainPage()
                .inputFieldPerson("personSurname", TestDataConstants.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("personName", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("personMidlname", TestDataConstants.InsuredPerson.MIDDLE_NAME_1)
                .clickMuiInputBase("snils", TestDataConstants.InsuredPerson.SNILS_1)
                .clickMuiInputBase("birthDate", TestDataConstants.InsuredPerson.Birthday);
    }

    @Step(value = "Период DSO-L (подпункт А/Б)")
    public void addPeriodDsol() {
        new MainPage()
                .clickRadioInput("А")
                .inputDateByIdViaJs("subitemAPeriodFrom", "01.01.2024")
                .inputDateByIdViaJs("subitemAPeriodTo", "30.06.2024")
                .inputDateByIdViaJs("subitemBPeriodFrom", "01.07.2024")
                .inputDateByIdViaJs("subitemBPeriodTo", "31.12.2024")
                .clickMuiInputBase("subpointANaletHours", "100")
                .clickMuiInputBase("subpointANaletMinutes", "0")
                .clickMuiInputBase("subpointBNaletHours", "50")
                .clickMuiInputBase("subpointBNaletMinutes", "30");
    }

    @Step(value = "Период DSO-U (представитель/не представитель/по списку)")
    public void addPeriodDsou() {
        new MainPage()
                .clickRadioInput("А")
                .inputDateByIdViaJs("dsouPresenterPeriodFrom", "01.01.2024")
                .inputDateByIdViaJs("dsouPresenterPeriodTo", "30.06.2024")
                .inputDateByIdViaJs("dsouNotPresenterPeriodFrom", "01.07.2024")
                .inputDateByIdViaJs("dsouNotPresenterPeriodTo", "31.12.2024")
                .inputDateByIdViaJs("dsouByListPeriodFrom", "01.01.2024")
                .inputDateByIdViaJs("dsouByListPeriodTo", "31.12.2024");
    }

    // ===================== ЕФС-1 (раздел СТАЖ) =====================

    @Step(value = "ЗЛ СТАЖА ЕФС-1 (ФИО/СНИЛС/ИНН)")
    public void createEfsStajPerson() {
        new MainPage()
                .inputFieldPerson("personSurname", TestDataConstants.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("personName", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("personMiddlename", TestDataConstants.InsuredPerson.MIDDLE_NAME_1)
                .clickMuiInputBase("personSnils", TestDataConstants.InsuredPerson.SNILS_1)
                .clickMuiInputBase("personInn", TestDataConstants.InsuredPerson.INN_1);
    }

    @Step(value = "Период стажа ЕФС-1")
    public void createEfsStajPeriod() {
        new MainPage()
                .inputDateByIdViaJs("experienceTimePeriodDateAt", "01.01.2024")
                .inputDateByIdViaJs("experienceTimePeriodDateTo", "31.12.2024");
    }

    @Step(value = "Подписать и отправить")
    public void submitAndSend() {
        submitAndSend(null);
    }

    @Step(value = "Подписать и отправить с провайдером: {providerName}")
    public void submitAndSend(String providerName) {
        new MainPage()
                .clickBtnPrimary("Подписать и отправить")
                .clickButtonModalDialog("Да");

        // Ожидаем появления кнопки «Подписать» — формирование отчёта может занять до 2 минут
        $x("//button[contains(@class, 'btn-primary')]//span[text() = 'Подписать']")
                .shouldBe(Condition.visible, REPORT_PROCESSING_WAIT);

        if (providerName != null && !providerName.isEmpty()) {
            logger.info("Выбор провайдера подписи: {}", providerName);
            chooseCriptoProvider();
        }

        new MainPage()
                .clickBtnPrimary("Подписать");
    }

    @Step("Выбор криптопровайдера")
    public void chooseCriptoProvider() {
        new MainPage()
                .clickSpanCloseDialog("КриптоПро")
                .clickButtonCloseDialog("Выбрать");
    }

    @Step(value = "Загрузка XML")
    public void sendXml(ReportXmlResource reportXmlResource) {
        new MainPage()
                .clickBtnSecondary("Загрузить отчет")
                .uploadFile(reportXmlResource.getPath());

        // Кнопка «Загрузить» активна только после прикрепления файла (может включиться с задержкой)
        $x("//div[@class = 'modal-content']//button[*[text() = 'Загрузить']]")
                .shouldBe(Condition.enabled, DEFAULT_WAIT);

        new MainPage()
                .clickButtonModal("Загрузить")
                .clickButtonModalDialog("Да");

        // Диагностика сбоя загрузки: сервер может вернуть ошибку (например, NOT_FOUND grpc-сервиса
        // или «Ошибка поиска страхователя в РС»), при этом модалка остаётся открытой и позже
        // блокирует навигацию. Падаем сразу и с текстом ошибки, а не с непонятным таймаутом.
        try {
            Selenide.Wait().withTimeout(Duration.ofSeconds(15)).until(d -> {
                var alerts = $$x("//div[contains(@class,'ps-alert-danger')]");
                for (int i = 0; i < alerts.size(); i++) {
                    if (alerts.get(i).isDisplayed()) {
                        return true;
                    }
                }
                return false;
            });

            String title = "";
            String details = "";
            var titles = $$x("//h4[contains(@class,'ps-alert-title')]");
            for (int i = 0; i < titles.size(); i++) {
                if (titles.get(i).isDisplayed()) {
                    title = titles.get(i).getText();
                    break;
                }
            }
            var texts = $$x("//p[contains(@class,'ps-alert-text')]");
            for (int i = 0; i < texts.size(); i++) {
                if (texts.get(i).isDisplayed()) {
                    details = texts.get(i).getText();
                    break;
                }
            }
            throw new AssertionError("Ошибка при загрузке XML ('" + reportXmlResource + "'): "
                    + (title.isEmpty() ? "" : title + ". ") + details);
        } catch (TimeoutException e) {
            // Ошибок не появилось — загрузка принята сервером
        }
    }
}