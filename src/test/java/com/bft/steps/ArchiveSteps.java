package com.bft.steps;

import com.bft.ui.pages.MainPage;
import com.codeborne.selenide.Condition;
import io.qameta.allure.Step;
import org.springframework.stereotype.Component;

import static com.bft.enums.TimeoutConstants.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

/**
 * Шаги для работы с запросами в архивные организации.
 */
@Component
public class ArchiveSteps {

    String getActualResult;

    @Step("Переход в реестр запросов архивной организации")
    public void openArchiveRequestsRegistry() {
        new MainPage()
                .openTab("ЛК Архивной Организации")
                .openTab("Реестр запросов");
    }

    @Step("Добавление исполнителя")
    public void addPerformer() {
        new MainPage()
                .openTab("ЛК Архивной Организации")
                .openTab("Реестр исполнителей")
                .waitTableToLoad()
                .clickButton("Добавить");
    }

    @Step("Заполнение исполнителя")
    public void fillPerformer() {
        $("[name=fullName]").setValue("Котова Екатерина Олеговна");
        $("[name=phoneNumber]").setValue("+79772684510");
        $("[name=division]").setValue("Тестовое государственное казенное учреждение Тестовой области");
        new MainPage().clickButtonModal("Подтвердить");
    }

    @Step("Переход во вкладку Реестр запросов")
    public void reestrZaprosov() {
        new MainPage()
                .openTab("ЛК Архивной Организации")
                .openTab("Реестр запросов")
                .openTab("Управление запросами");
    }

    @Step("Поиск исполнителя по имени: {performerName}")
    public void searchPerformer(String performerName) {
        new MainPage()
                .clickInputLabel("Исполнитель в архивной организации", performerName)
                .clickMainButton("Применить");
    }

    @Step("Поиск запроса в фильтре и открытие карточки")
    public void searchRequest1(String numberRequest) {
        new MainPage()
                .clickMInputLabel("requestNumber", numberRequest)
                .clickMainButton("Применить")
                .openTable();
    }

    @Step("Назначение исполнителя")
    public void assignPerformer() {
        new MainPage()
                .clickBtnSecondary1("Назначить исполнителя")
                .clickMuiInputLabelID("mui-38", "Слатова Марина Николаевна")
                .clickSpanButton("Подтвердить");
    }

    @Step("Выбор ответа на запрос")
    public void answerRequest() {
        new MainPage()
                .clickRadioButton("Положительный");
    }

    @Step("Заполнение таблицы Период работы, иной деятельности")
    public void addPeriodWork() {
        new MainPage()
                .clickButton("Добавить")
                .inputField("Вид деятельности", "Основной")
                .inputDateLabel("workPeriodStartDate", "01-01-2000")
                .inputDateLabel("workPeriodEndDate", "01-01-2000")
                .clickMuiInputLabel("Кадровое мероприятие", "ПРИЕМ")
                .inputField("Структурное подразделение", "Менеджмент")
                .inputField("Должность", "Менеджер")
                .clickMuiInputLabel("Особый период", "Время простоя по вине работодателя")
                .inputField("Дополнительные сведения", "Отпуск")
                .inputField("Распорядительный документ", "Приказ")
                .inputDateLabel("administrativeDocumentDate", "01-01-2024")
                .inputField("Номер документа", "Приказ 11")
                .clickButtonModal("Подтвердить");
    }

    @Step("Заполнение таблицы сведений о загранкомандировке")
    public void addBusinessTrip() {
        new MainPage()
                .clickSecondButton("Добавить")
                .inputDateLabel("tripPeriodStartDate", "01-01-2000")
                .inputDateLabel("tripPeriodEndDate", "01-01-2000")
                .clickInputLabel("Наименование организации", "ООО Котовасия")
                .clickMuiInputLabel("Государство пребывания", "АНТАРКТИДА")
                .clickRadioButton("На работу")
                .inputField("Распорядительный документ", "Приказ")
                .inputDateLabel("administrativeDocumentDate", "01-01-2024")
                .inputField("Номер документа", "Приказ 21")
                .clickButtonModal("Подтвердить");
    }

    @Step("Заполнение полей ответа")
    public void fillAnswer() {
        new MainPage()
                .inputLabelTextarea("responseComment-label", "responseComment", "Тестовый длинный текст тут должен быть")
                .inputLabelTextarea("documentRequisites-label", "documentRequisites", "124")
                .clickInputLabel("Номер ответа", "7-45");
    }

    @Step("Передать запрос на подписание")
    public void submitSigning() {
        new MainPage()
                .clickSpanButton("Передать на подписание");
        $x("//span[contains(text(), 'Подписать и отправить')]")
                .shouldBe(Condition.visible, LONG_WAIT);
        new MainPage()
                .clickSpanButton("Подписать и отправить");
        $x("//div[@class = 'item-footer']//button[span[text() = 'Подписать']]")
                .shouldBe(Condition.visible, LONG_WAIT);
        new MainPage()
                .clickFooterSpanButton("Подписать");
    }

    @Step("Создание запроса")
    public void createZaprosRPU() {
        new MainPage()
                .openTab("Реестр получателей услуг")
                .openTab("Реестр запросов в архивы")
                .clickButton("Создать запрос");
    }

    @Step("Заполнение блока ОСНОВНЫЕ СВЕДЕНИЯ О ЗАПРОСЕ")
    public void genegalInfo() {
        $x("//input[@type='checkbox']").shouldBe(Condition.visible, DEFAULT_WAIT);
        new MainPage()
                .clickCheckbox("Муниципальные")
                .clickCheckbox("Региональные")
                .clickMuiInputLabel("Регион", "Ярославская область")
                .clickMuiInputLabel("Архивная организация",
                        "Государственное казенное учреждение Ярославской области «Государственный архив Ярославской области»");
    }

    @Step("Заполнение блока СВЕДЕНИЯ О ГРАЖДАНИНЕ")
    public void infoCitizen() {
        new MainPage()
                .clickInputLabel("ФИО гражданина", "Котов Олег Олегович")
                .clickMInputLabel("snils-label", "351-818-056-74")
                .inputDateLabel("birthDate-label", "01-01-2000");
    }

    @Step("Заполнение блока ДАННЫЕ ОРГАНИЗАЦИИ")
    public void organizationData() {
        new MainPage()
                .clickInputLabel("Наименование организации", "ООО Котовасия");
    }

    @Step("Заполнение блока ОБЩИЙ ПЕРИОД РАБОТЫ")
    public void totalPeriod() {
        new MainPage()
                .inputDateLabel("workStartDate-label", "01-01-2000")
                .inputDateLabel("workEndDate-label", "01-01-2024")
                .inputDateLabel("dismissalDate-label", "01-01-2024");
    }

    @Step("Заполнение блока ВИД ФОРМЫ ЗАНЯТОСТИ")
    public void typeFormEmployment() {
        new MainPage()
                .clickInputLabel("Вид формы занятости", "Основной");
    }

    @Step("Заполнение блока ТЕМА ЗАПРОСА Период работы, иной деятельности")
    public void requestSubjectPeriodWork() {
        new MainPage()
                .clickMuiInputLabel("Тема запроса", "Период работы, иной деятельности");
    }

    @Step("Сохранить запрос")
    public void saveRequest() {
        new MainPage()
                .clickButton("Сохранить");
    }

    @Step("Подписать запрос")
    public void signRequest() {
        new MainPage()
                .clickButton("Подписать");
        $x("(//span[contains(text(), 'Подписать')])[2]")
                .shouldBe(Condition.visible, LONG_WAIT);
        new MainPage()
                .clickSecondSpanButton("Подписать");
    }

    @Step("Выбор сертификата")
    public void selectCertificate() {
        new MainPage()
                .chooseLastProvider()
                .clickButtonProviderDialog("Подписать");
        $x("//div[@class = 'provider-dialog']").should(Condition.disappear, LONG_WAIT);
        $x("//div[@class = 'item-footer']//button[text() = 'Готово']")
                .shouldBe(Condition.visible, DEFAULT_WAIT);
        new MainPage()
                .clickFooterButton("Готово");
    }

    @Step("Копируем номер запроса и отправляем запрос")
    public void get_nomber_send_request() {
        MainPage mainPage = new MainPage();
        mainPage.getNomberRequest();
        getActualResult = mainPage.getActualResult();
        $x("//span[contains(text(), 'Отправить запрос')]")
                .shouldBe(Condition.visible, DEFAULT_WAIT);
        mainPage.clickSpanButton("Отправить запрос");
        $x("//body").shouldBe(Condition.visible, SHORT_WAIT);
    }
}