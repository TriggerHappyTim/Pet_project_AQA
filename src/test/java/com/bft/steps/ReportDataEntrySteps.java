package com.bft.steps;

import com.bft.enums.ReportXmlResource;
import com.bft.security.masking.SecureLogger;
import com.bft.testdata.TestDataConstants;
import com.bft.ui.component.GracePeriodDialogComponent;
import com.bft.ui.pages.MainPage;
import com.codeborne.selenide.Condition;
import io.qameta.allure.Step;
import org.springframework.stereotype.Component;

import static com.bft.enums.TimeoutConstants.*;
import static com.codeborne.selenide.Selenide.$x;

@Component
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
                .inputFieldPerson("directorLastName-label", TestDataConstants.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("directorFirstName-label", TestDataConstants.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("directorMiddleName-label", TestDataConstants.InsuredPerson.MIDDLE_NAME_1)
                .clickSpanId("pgs-help-label", TestDataConstants.InsuredPerson.JOB)
                .clickMuiInputBase("personCount-label", "1");
        fillBasisSectionISH();
    }

    @Step(value = "Секция Основание СЗВ-ИСХ")
    public void fillBasisSectionISH() {
        new MainPage()
                .clickMuiInputBase("basisTotalStaff", "1")
                .clickMuiInputBase("basisActualEmployees", "1");
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
                .clickMInputLabel("activity_basisEvent_0_date", "01.01.2025")
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
                .clickMInputLabel("documentList_0_date", "01.01.2025")
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
    }
}