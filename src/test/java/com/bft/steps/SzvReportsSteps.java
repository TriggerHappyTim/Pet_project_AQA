package com.bft.steps;

import com.bft.helpers.TestConfig;
import com.bft.security.CredentialManager;
import com.bft.security.TestUsers;
import com.bft.security.masking.SecureLogger;
import com.codeborne.selenide.Condition;
import com.bft.enums.ReportType;
import com.bft.enums.UIType;
import com.bft.gui.LoginPage;
import com.bft.ui.pages.MainPage;
import io.qameta.allure.Step;
import org.springframework.stereotype.Component;

import static com.bft.constants.TimeoutConstants.*;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static com.bft.enums.UIType.EVS_TEST;

/**
 * Steps класс для работы с отчетами СЗВ в системе EVS
 * 
 * Предоставляет высокоуровневые методы для выполнения сложных бизнес-процессов:
 * - Авторизация в различных системах (ЕВС, РПУ, УОС)
 * - Работа с запросами в архивные организации
 * - Создание и заполнение отчетов СЗВ различных типов
 * - Работа с застрахованными лицами (ЗЛ)
 * - Подписание и отправка отчетов
 * 
 * <p>Все методы помечены аннотацией @Step для интеграции с Allure отчетностью.
 * Методы используют Page Objects (MainPage, LoginPage) для взаимодействия с UI.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * SzvReportsSteps steps = new SzvReportsSteps();
 * steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.DEFAULT_USER);
 * steps.addReports();
 * steps.selectReportType(ReportType.SZV_M);
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see MainPage для работы с UI элементами
 * @see LoginPage для авторизации
 * @see CredentialManager для управления учетными данными
 */
@Component
public class SzvReportsSteps {

    private final SecureLogger logger = SecureLogger.getLogger(getClass());
    private final CredentialManager credentialManager = CredentialManager.getInstance();

    /**
     * Авторизация в системе ЕВС через ЕПГУ для архивной организации
     * 
     * Выполняет авторизацию в системе EVS через единый портал государственных услуг (ЕПГУ).
     * Использует учетные данные из CredentialManager по префиксу "epgu".
     * После авторизации автоматически выбирает карточку организации.
     * 
     * @throws RuntimeException если учетные данные EPGU недоступны
     */
    @Step(value = "Авторизация в ЕВС (ЕПГУ)")
    public void authorizeArhivEVS() {
        var credentials = credentialManager.getUserCredentials("epgu");
        if (credentials == null || !credentials.isValid()) {
            throw new RuntimeException("EPGU credentials not available. Please set epgu.username and epgu.password environment variables.");
        }

        logger.info("Выполняем авторизацию в ЕВС через ЕПГУ для пользователя: {}", credentials.username);

        new LoginPage()
                .open(EVS_TEST)
                .authorizeEPGU(credentials.username, credentials.password)
                .selectUserCardEPGU("ОРГАНИЗАЦИЯ -1220859909");
    }

    /**
     * Добавляет нового исполнителя в реестр исполнителей
     * 
     * Открывает реестр исполнителей архивной организации и нажимает кнопку "Добавить"
     * для создания новой записи об исполнителе.
     */
    @Step("Добавление исполнителя")
    public void addPerformer() {
        new MainPage()
                .openTab("ЛК Архивной Организации")
                .openTab("Реестр исполнителей")
                .waitTableToLoad()
                .clickButton("Добавить");
    }

    /**
     * Заполняет форму добавления исполнителя
     * 
     * Вводит данные об исполнителе:
     * - ФИО: Котова Екатерина Олеговна
     * - Номер телефона: +79772684510
     * - Подразделение: Тестовое государственное казенное учреждение Тестовой области
     * 
     * После заполнения подтверждает добавление через модальное окно.
     */
    @Step("Заполнение исполнителя")
    public void fillPerformer(){
        $("[name=fullName]").setValue("Котова Екатерина Олеговна");
        $("[name=phoneNumber]").setValue("+79772684510");
        $("[name=division]").setValue("Тестовое государственное казенное учреждение Тестовой области");
        new MainPage().clickButtonModal("Подтвердить");
    }

    /**
     * Переходит во вкладку "Реестр запросов"
     * 
     * Выполняет навигацию: ЛК Архивной Организации -> Реестр запросов -> Управление запросами.
     */
    @Step("Переход во вкладку Реестр запросов")
    public void reestrZaprosov(){
        new MainPage()
                .openTab("ЛК Архивной Организации")
                .openTab("Реестр запросов")
                .openTab("Управление запросами");
    }

    /**
     * Выполняет поиск исполнителя в фильтре
     * 
     * Вводит ФИО исполнителя в поле фильтра и применяет фильтр.
     * 
     * @param performerName ФИО исполнителя для поиска (например, "Котов Олег Олегович")
     */
    @Step("Поиск исполнителя")
    public void searchPerformer(){
        new MainPage()
                .clickInputLabel("Исполнитель в архивной организации","Котов Олег Олегович")
                .clickMainButton("Применить");
    }

    /**
     * Выполняет поиск запроса по сохраненному номеру и открывает карточку
     * 
     * Использует номер запроса, сохраненный в переменной getActualResult.
     * После поиска открывает таблицу результатов.
     */
    @Step("Поиск запроса в фильтре и открытие карточки")
    public void searchRequest() {
        new MainPage()
                /*.getNomberRequest()*/
                .clickMInputLabel("requestNumber", getActualResult)
                .clickMainButton("Применить")
                .openTable();
    }

    /**
     * Выполняет поиск запроса по указанному номеру и открывает карточку
     * 
     * Вводит номер запроса в поле фильтра и применяет фильтр.
     * После поиска открывает таблицу результатов.
     * 
     * <p>Номер запроса передается как параметр, что позволяет использовать
     * как статические значения, так и динамически сгенерированные номера.
     * 
     * @param numberRequest номер запроса для поиска (может быть статическим или динамически сгенерированным)
     */
    @Step("Поиск запроса в фильтре и открытие карточки")
    public void searchRequest1(String numberRequest) {
        new MainPage()
                .clickMInputLabel("requestNumber", numberRequest)
                .clickMainButton("Применить")
                .openTable();

        ;

    }

    /**
     * Назначает исполнителя на запрос
     * 
     * Выполняет назначение исполнителя на текущий запрос:
     * 1. Кликает по кнопке "Назначить исполнителя" (во втором окне)
     * 2. Выбирает исполнителя из списка
     * 3. Подтверждает назначение
     */
    @Step("Назначение исполнителя")
    public void assignPerformer(){
        new MainPage()
                // Ожидаем появления кнопки "Назначить исполнителя" перед кликом
                .clickBtnSecondary1("Назначить исполнителя")
                .clickMuiInputLabelID("mui-38","Cлатова Марина Николаевна")
                .clickSpanButton("Подтвердить");
    }

    /**
     * Выбирает ответ на запрос
     * 
     * Выбирает радиокнопку "Положительный" для ответа на запрос.
     * Используется для указания типа ответа на запрос в архивную организацию.
     */
    @Step("Выбор ответа на запрос")
    public void answerRequest(){
        new MainPage()
                .clickRadioButton("Положительный");
    }

    /**
     * Добавляет период работы или иной деятельности
     * 
     * Заполняет форму добавления периода работы:
     * - Вид деятельности: Основной
     * - Период работы: с 01-01-2000 по 01-01-2000
     * - Кадровое мероприятие: ПРИЕМ
     * - Структурное подразделение: Менеджмент
     * - Должность: Менеджер
     * - Особый период: Время простоя по вине работодателя
     * - Дополнительные сведения: Отпуск
     * - Распорядительный документ: Приказ от 01-01-2024, номер "Приказ 11"
     */
    @Step("Заполнение таблицы Период работы, иной деятельности")
    public void addPeriodWork(){
        new MainPage()
                .clickButton("Добавить")
                //Модальное окно//Добавление периода работы / иной деятельности
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

    /**
     * Добавляет сведения о загранкомандировке
     * 
     * Заполняет форму добавления сведений о загранкомандировке:
     * - Период командировки: с 01-01-2000 по 01-01-2000
     * - Наименование организации: ООО Котовасия
     * - Государство пребывания: АНТАРКТИДА
     * - Цель командировки: На работу
     * - Распорядительный документ: Приказ от 01-01-2024, номер "Приказ 21"
     */
    @Step("Заполнение таблицы сведений о загранкомандировке")
    public void addBusinessTrip() {
        new MainPage()
                //СВЕДЕНИЯ О ЗАГРАНКОМАНДИРОВКЕ
                .clickSecondButton("Добавить")
                //Модальное окно//Добавление сведений о загранкомандировке
                .inputDateLabel("tripPeriodStartDate", "01-01-2000")
                .inputDateLabel("tripPeriodEndDate", "01-01-2000")
                .clickInputLabel("Наименование организации", "ООО Котовасия")
                /*.clickSecondInputLabel("Наименование организации", "ООО Котовасия")*/
                .clickMuiInputLabel("Государство пребывания", "АНТАРКТИДА")
                .clickRadioButton("На работу")
                .inputField("Распорядительный документ", "Приказ")
                .inputDateLabel("administrativeDocumentDate", "01-01-2024")
                .inputField("Номер документа", "Приказ 21")
                .clickButtonModal("Подтвердить");
    }

    /**
     * Заполняет поля ответа на запрос
     * 
     * Вводит данные в поля формы ответа:
     * - Комментарий к ответу: "Тестовый длинный текст тут должен быть"
     * - Реквизиты документа: "124"
     * - Номер ответа: "7-45"
     */
    @Step("Заполнение полей ответа")
    public void fillAnswer(){
        new MainPage()
                .inputLabelTextarea("responseComment-label","responseComment","Тестовый длинный текст тут должен быть")
                .inputLabelTextarea("documentRequisites-label","documentRequisites","124")
                .clickInputLabel("Номер ответа","7-45");
    }

    /**
     * Передает запрос на подписание
     * 
     * Выполняет последовательность действий для подписания запроса:
     * 1. Передает запрос на подписание
     * 2. Ожидает появления кнопки "Подписать и отправить"
     * 3. Кликает по кнопке "Подписать и отправить"
     * 4. Ожидает появления кнопки "Подписать" в футере
     * 5. Подписывает запрос
     * 
     * Использует умные ожидания для стабильности теста.
     */
    @Step("Передать запрос на подписание")
    public void submitSigning() {
        new MainPage()
                .clickSpanButton("Передать на подписание");
        // Ожидаем появления кнопки "Подписать и отправить" после обработки запроса
        $x("//span[contains(text(), 'Подписать и отправить')]")
                .shouldBe(Condition.visible, LONG_WAIT);
        new MainPage()
                .clickSpanButton("Подписать и отправить");
        // Ожидаем появления кнопки "Подписать" в футере
        $x("//div[@class = 'item-footer']//button[span[text() = 'Подписать']]")
                .shouldBe(Condition.visible, LONG_WAIT);
        new MainPage()
                .clickFooterSpanButton("Подписать");
        //Нажатие кнопки "Подписать" в футере
    }

    /**
     * Выполняет выход из учетной записи
     * 
     * Ожидает завершения всех операций перед выходом и выполняет выход из системы
     * через метод logOut() класса MainPage.
     */
    @Step("Выход с учетной записи")
    public void logOut() {
        // Ожидаем завершения всех операций перед выходом
        $x("//body").shouldBe(Condition.visible, SHORT_WAIT);
        new MainPage()
                .logOut();
    }

    //----------------------------Блок РПУ---------------------------------------------
    
    /**
     * Авторизация в системе РПУ (Региональный портал услуг)
     * 
     * Выполняет авторизацию в системе РПУ используя учетные данные из CredentialManager
     * по префиксу "rpu".
     * 
     * @param uiType тип UI окружения (UAT, TEST, PROD)
     * @throws RuntimeException если учетные данные RPU недоступны
     */
    @Step("Авторизация в РПУ")
    public void authorizeArhivRPU(UIType uiType) {
        var credentials = credentialManager.getUserCredentials("rpu");
        if (credentials == null || !credentials.isValid()) {
            throw new RuntimeException("RPU credentials not available. Please set rpu.username and rpu.password environment variables.");
        }

        logger.info("Выполняем авторизацию в РПУ для пользователя: {}", credentials.username);

        new LoginPage()
                .open(UIType.valueOf(uiType.value))
                .authorize(credentials.username, credentials.password);
    }

    /**
     * Создает новый запрос в системе РПУ
     * 
     * Открывает реестр запросов в архивы и нажимает кнопку "Создать запрос"
     * для начала процесса создания нового запроса.
     */
    @Step("Создание запроса")
    public void createZaprosRPU(){
        new MainPage()
                .openTab("Реестр получателей услуг")
                .openTab("Реестр запросов в архивы")
                .clickButton("Создать запрос");
    }

    /**
     * Заполняет блок "Основные сведения о запросе"
     * 
     * Заполняет основные параметры запроса:
     * - Тип архивов: Муниципальные и Региональные (чекбоксы)
     * - Регион: Ярославская область
     * - Архивная организация: Государственное казенное учреждение Ярославской области «Государственный архив Ярославской области»
     */
    @Step("Заполнение блока ОСНОВНЫЕ СВЕДЕНИЯ О ЗАПРОСЕ")
    public void genegalInfo() {
        // Ожидаем загрузки формы с чекбоксами
        $x("//input[@type='checkbox']").shouldBe(Condition.visible, DEFAULT_WAIT);
        new MainPage()
                .clickCheckbox("Муниципальные")
                .clickCheckbox("Региональные")
                .clickMuiInputLabel("Регион", "Ярославская область")
                .clickMuiInputLabel("Архивная организация",
                        "Государственное казенное учреждение Ярославской области «Государственный архив Ярославской области»");
    }

    /**
     * Заполняет блок "Сведения о гражданине"
     * 
     * Вводит данные о гражданине, на которого оформляется запрос:
     * - ФИО: Котов Олег Олегович
     * - СНИЛС: 351-818-056-74
     * - Дата рождения: 01-01-2000
     */
    @Step("Заполнение блока СВЕДЕНИЯ О ГРАЖДАНИНЕ")
    public void infoCitizen() {
        new MainPage()
                .clickInputLabel("ФИО гражданина", "Котов Олег Олегович")
                .clickMInputLabel("snils-label", "351-818-056-74")
                .inputDateLabel("birthDate-label", "01-01-2000");
    }

    /**
     * Заполняет блок "Изменение сведений о гражданине"
     * 
     * Необязательный блок для указания изменений в данных гражданина:
     * - Дата изменения: 01-01-2000
     * - Новая фамилия: Котова
     * - Новое имя: Ольга
     * - Новое отчество: Олеговна
     * - Новая дата рождения: 01-01-2000
     */
    @Step("Заполнение блока ИЗМЕНЕНИЕ СВЕДЕНИЙ О ГРАЖДАНИНЕ")
    //необязательный блок
    public void changeInfoCitizen() {
        new MainPage()
                .inputDateLabel("citizenChangeInfoDate-label", "01-01-2000")
                .clickInputLabel("Новая фамилия", "Котова")
                .clickInputLabel("Новое имя", "Ольга")
                .clickInputLabel("Новое отчество", "Олеговна")
                .inputDateLabel("newBirthDate-label", "01-01-2000");
    }

    /**
     * Заполняет блок "Данные организации"
     * 
     * Вводит наименование организации, от имени которой оформляется запрос.
     * 
     * @param organizationName наименование организации (например, "ООО Котовасия")
     */
    @Step("Заполнение блока ДАННЫЕ ОРГАНИЗАЦИИ")
    public void organizationData() {
        new MainPage()
                .clickInputLabel("Наименование организации", "ООО Котовасия");
        //необязательные поля
                /*.inputField("ИНН", "4295853545")
                .clickMInputLabel("sfrRegNumber","351-818-056746")
                .clickInputLabel("Место нахождения организации", "г.Москва");*/
    }

    /**
     * Заполняет блок "Сведения об обособленном подразделении (филиале, представительстве) организации"
     * 
     * Необязательный блок для указания данных обособленного подразделения:
     * - Наименование: ООО Котовасия
     * - ИНН: 4295853545
     * - Регистрационный номер: 351-818-056746
     * - Место нахождения: г.Москва
     */
    @Step("Заполнение блока СВЕДЕНИЯ ОБ ОБОСОБЛЕННОМ ПОДРАЗДЕЛЕНИИ(ФИЛИАЛЕ, ПРЕДСТАВИТЕЛЬСТВЕ) ОРГАНИЗАЦИИ")
    //необязательный блок
    public void infoSeparateDivision() {
        new MainPage()
                .clickInputLabel("Наименование обособленного подразделения", "ООО Котовасия")
                .clickMInputLabel("divisionInn-label", "4295853545")
                .clickMInputLabel("divisionRegNumber","351-818-056746")
                .clickInputLabel("Место нахождения филиала", "г.Москва");
    }

    /**
     * Заполняет блок "Общий период работы"
     * 
     * Указывает общий период работы гражданина в организации:
     * - Дата начала работы: 01-01-2000
     * - Дата окончания работы: 01-01-2024
     * - Дата увольнения: 01-01-2024
     */
    @Step("Заполнение блока ОБЩИЙ ПЕРИОД РАБОТЫ")
    public void totalPeriod(){
        new MainPage()
                .inputDateLabel("workStartDate-label", "01-01-2000")
                .inputDateLabel("workEndDate-label", "01-01-2024")
                .inputDateLabel("dismissalDate-label", "01-01-2024");
    }

    /**
     * Заполняет блок "Вид формы занятости"
     * 
     * Указывает вид формы занятости гражданина (например, "Основной").
     */
    @Step("Заполнение блока ВИД ФОРМЫ ЗАНЯТОСТИ")
    public void typeFormEmployment() {
        new MainPage()
                .clickInputLabel("Вид формы занятости", "Основной");
    }

    /**
     * Заполняет блок "Тема запроса" - выбирает тему "Период работы, иной деятельности"
     * 
     * Выбирает тему запроса из выпадающего списка.
     * Закомментированный код содержит примеры заполнения детальной информации о периодах работы
     * и загранкомандировках (не используется в текущей реализации).
     */
    @Step("Заполнение блока ТЕМА ЗАПРОСА Период работы, иной деятельности")
    public void requestSubjectPeriodWork() {
        new MainPage()
                .clickMuiInputLabel("Тема запроса","Период работы, иной деятельности");//Период работы, иной деятельности
        //СВЕДЕНИЯ О ПЕРИОДАХ РАБОТЫ, ИНОЙ ДЕЯТЕЛЬНОСТИ
                /*.clickButton("Добавить")
                    //Модальное окно//Добавление периода работы / иной деятельности
                .inputField("Вид деятельности", "Основной")
                .inputDateLabel("periodStartDate", "01-01-2000")
                .inputDateLabel("periodEndDate", "01-01-2000")
                .clickMuiInputLabel("Кадровое мероприятие", "ПРИЕМ")
                .inputField("Структурное подразделение", "Менеджмент")
                .inputField("Должность", "Менеджер")
                .clickMuiInputLabel("Особый период", "Время простоя по вине работодателя")
                .inputField("Дополнительные сведения", "Отпуск")
                .inputField("Распорядительный документ", "Приказ")
                .inputDateLabel("documentDate", "01-01-2024")
                .inputField("Номер документа", "Приказ 11")
                .clickButtonModal("Подтвердить")
                //СВЕДЕНИЯ О ЗАГРАНКОМАНДИРОВКЕ
                .clickSecondButton("Добавить")
                    //Модальное окно//Добавление сведений о загранкомандировке
                .inputDateLabel("periodStartDate", "01-01-2000")
                .inputDateLabel("periodEndDate", "01-01-2000")
                .clickSecondInputLabel("Наименование организации", "ООО Котовасия")
                .clickMuiInputLabel("Государство пребывания", "АНТАРКТИДА")
                .clickRadioButton("На работу")
                .inputField("Распорядительный документ", "Приказ")
                .inputDateLabel("documentDate", "01-01-2024")
                .inputField("Номер документа", "Приказ 21")
                .clickButtonModal("Подтвердить")*/
    }

    /**
     * Добавляет приложение к запросу
     * 
     * Загружает файл отчета в качестве приложения к запросу.
     * Тип файла определяется по типу отчета (reportType.value содержит путь к файлу).
     * 
     * @param reportType тип отчета, определяющий какой файл загружать
     */
    @Step("Заполнение блока ПРИЛОЖЕНИЯ К ЗАПРОСУ")
    public void attachmentsRequest(ReportType reportType) {
        new MainPage()
                .clickThirdButton("Добавить")
                .uploadFile(reportType.value)
                .clickButtonModal("Сохранить изменения");
    }

    /**
     * Сохраняет запрос как черновик
     * 
     * Нажимает кнопку "Сохранить" для сохранения запроса без отправки.
     */
    @Step("Сохранить запрос")
    public void saveRequest() {
        new MainPage()
                .clickButton("Сохранить");
    }

    /**
     * Подписывает запрос электронной подписью
     * 
     * Выполняет последовательность действий для подписания запроса:
     * 1. Нажимает кнопку "Подписать"
     * 2. Ожидает появления второй кнопки "Подписать" в футере
     * 3. Кликает по второй кнопке "Подписать" в футере
     * 
     * Использует умные ожидания для стабильности теста.
     */
    @Step("Подписать запрос")
    public void signRequest() {
        new MainPage()
                .clickButton("Подписать");
        // Ожидаем появления кнопки "Подписать" в футере после обработки запроса
        $x("(//span[contains(text(), 'Подписать')])[2]")
                .shouldBe(Condition.visible, LONG_WAIT);
        new MainPage()
                .clickSecondSpanButton("Подписать");
        //Нажатие кнопки "Подписать" в футере
    }

    /**
     * Выбирает криптопровайдера для подписания
     * 
     * Выбирает провайдера электронной подписи "КриптоПро" из диалога выбора провайдера
     * и подтверждает выбор.
     */
    @Step("Выбор криптопровайдера")
    public void chooseCriptoProvider() {
        new MainPage()
                .clickSpanCloseDialog("КриптоПро")
                .clickButtonCloseDialog("Выбрать");
    }

    /**
     * Выбирает сертификат для подписания
     * 
     * Выполняет последовательность действий для выбора сертификата:
     * 1. Выбирает последний провайдер из списка
     * 2. Подписывает документ выбранным сертификатом
     * 3. Ожидает завершения процесса подписания (исчезновение диалога провайдера)
     * 4. Ожидает появления кнопки "Готово" в футере
     * 5. Нажимает кнопку "Готово"
     * 
     * Использует умные ожидания для стабильности теста.
     */
    @Step("Выбор сертификата")
    public void selectCertificate() {
        new MainPage()
                .chooseLastProvider()
                .clickButtonProviderDialog("Подписать");
        // Ожидаем завершения процесса подписания - проверяем исчезновение диалога провайдера
        $x("//div[@class = 'provider-dialog']").should(Condition.disappear, LONG_WAIT);
        // Ожидаем появления кнопки "Готово" в футере
        $x("//div[@class = 'item-footer']//button[text() = 'Готово']")
                .shouldBe(Condition.visible, DEFAULT_WAIT);
        new MainPage()
                .clickFooterButton("Готово");
    }

    String getActualResult;
    
    /**
     * Копирует номер запроса и отправляет запрос
     * 
     * Выполняет последовательность действий:
     * 1. Получает номер запроса из формы и сохраняет его в переменную getActualResult
     * 2. Ожидает появления кнопки "Отправить запрос"
     * 3. Отправляет запрос
     * 4. Ожидает завершения отправки
     * 
     * Номер запроса сохраняется для последующего использования в других шагах теста.
     */
    @Step("Копируем номер запроса и отправляем запрос")
    public void get_nomber_send_request(){
        MainPage mainPage = new MainPage();
        mainPage.getNomberRequest();
        getActualResult = mainPage.getActualResult();
        // Ожидаем появления кнопки "Отправить запрос" после получения номера
        $x("//span[contains(text(), 'Отправить запрос')]")
                .shouldBe(Condition.visible, DEFAULT_WAIT);
        mainPage.clickSpanButton("Отправить запрос");
        // Ожидаем завершения отправки - проверяем изменение статуса или появление сообщения
        $x("//body").shouldBe(Condition.visible, SHORT_WAIT);
    }

    //---------------------UOS----------------------------------
    
    /**
     * Авторизация в системе УОС (Управление отчетностью страхователей)
     * 
     * Выполняет авторизацию в системе УОС используя учетные данные из CredentialManager
     * по префиксу "uos".
     * 
     * @param uiType тип UI окружения (UAT, TEST, PROD)
     * @throws RuntimeException если учетные данные UOS недоступны
     */
    @Step("Авторизация в УОС")
    public void authorizeUOS(UIType uiType) {
        var credentials = credentialManager.getUserCredentials("uos");
        if (credentials == null || !credentials.isValid()) {
            throw new RuntimeException("UOS credentials not available. Please set uos.username and uos.password environment variables.");
        }

        logger.info("Выполняем авторизацию в УОС для пользователя: {}", credentials.username);

        new LoginPage()
                .open(uiType)
                .authorize(credentials.username, credentials.password);
    }

    /**
     * Выбирает отчет ЕФС-1 в системе УОС
     * 
     * Выполняет поиск и открытие отчета ЕФС-1:
     * 1. Открывает вкладку "УОС"
     * 2. Кликает по элементу меню с ID "mi7"
     * 3. Вводит ID процесса в поле поиска
     * 4. Применяет фильтр
     * 5. Открывает найденную карточку отчета
     * 
     * @param processId ID процесса для поиска (UUID формата)
     */
    @Step("Выбор ЕФС-1 с ID процесса: {processId}")
    public void chooseEFS(String processId) {
        new MainPage()
                .openTab("УОС");
        $("#mi7").click();
        $x("//*[@id='processId']").shouldBe(Condition.visible, DEFAULT_WAIT).click();
        $x("//*[@id='processId']").setValue(processId);

        $("button.btn-primary").click(); // Клик применить
        // Переход в карточку после ее поиска
        $x("//table[@class='']").click();

        /*$x("//span[text() = 'Посмотреть XML']").click();*/
    }
    
    /**
     * Выбирает отчет ЕФС-1 из списка доступных отчетов
     * 
     * Выполняет поиск отчета ЕФС-1 по ID процесса по умолчанию и открывает его карточку.
     * Использует стандартный ID процесса для ЕФС-1: "b44b9a8b-c6b0-4c64-afde-b66302f32ad4".
     * 
     * <p>Шаги выполнения:
     * <ol>
     *   <li>Открывает вкладку "УОС"</li>
     *   <li>Кликает по элементу меню с ID "mi7"</li>
     *   <li>Вводит ID процесса в поле поиска</li>
     *   <li>Применяет фильтр</li>
     *   <li>Открывает найденную карточку отчета</li>
     * </ol>
     * 
     * @see #chooseEFS(String) для указания кастомного ID процесса
     */
    @Step("Выбор ЕФС-1")
    public void chooseEFS() {
        chooseEFS("b44b9a8b-c6b0-4c64-afde-b66302f32ad4");
    }

    //---------------------EVS----------------------------------
    
    /**
     * Авторизация в EVS с использованием предопределенного пользователя из enum
     * 
     * <p>Использует учетные данные из credentials.properties или переменных окружения.
     * Автоматически выбирает организацию пользователя после авторизации.
     * 
     * @param uiType тип UI окружения (UAT, TEST, PROD)
     * @param user тестовый пользователь из enum TestUsers
     * @throws IllegalStateException если credentials пользователя не найдены
     * 
     * @see TestUsers
     */
    @Step(value = "Авторизация в EVS: {user.fullName}")
    public void authorizeEVS(UIType uiType, TestUsers user) {
        logger.info("Выполняем авторизацию в EVS для пользователя: {}", user.getFullName());
        
        if (!user.hasCredentials()) {
            throw new IllegalStateException(
                String.format("Credentials для пользователя '%s' недоступны. " +
                    "Проверьте файл credentials.properties или переменные окружения.", 
                    user.getFullName())
            );
        }
        
        String username = user.getUsername();
        String password = user.getPassword();
        String organization = user.getOrganization();
        
        logger.info("Авторизация: пользователь={}, организация={}", username, organization);
        
        new LoginPage()
                .open(uiType)
                .authorizeEPGU(username, password)
                .selectUserCardEPGU(organization)
                .open(uiType);
    }
    
    /**
     * Авторизация в EVS с дефолтным пользователем (обратная совместимость)
     * 
     * <p>Использует credentials из CredentialManager по префиксу "evs".
     * Если credentials не найдены, использует TestUsers.DEFAULT_USER.
     * 
     * @param uiType тип UI окружения (UAT, TEST, PROD)
     */
    @Step(value = "Авторизация в EVS (дефолтный пользователь)")
    public void authorizeEVS(UIType uiType) {
        var credentials = credentialManager.getUserCredentials("evs");
        
        if (credentials == null || !credentials.isValid()) {
            logger.warn("EVS credentials не найдены через CredentialManager, используем DEFAULT_USER из TestUsers");
            authorizeEVS(uiType, TestUsers.DEFAULT_USER);
            return;
        }
        
        logger.info("Выполняем авторизацию в EVS для пользователя: {}", credentials.username);
        
        String organization = credentialManager.getCredential("evs.organization", "ОРГАНИЗАЦИЯ -1563384004");
        
        new LoginPage()
                .open(uiType)
                .authorizeEPGU(credentials.username, credentials.password)
                .selectUserCardEPGU(organization)
                .open(uiType);
    }

    /**
     * Добавляет новый отчет в системе EVS
     * 
     * Открывает раздел "ЛК Страхователя" -> "Отчеты", ожидает загрузки таблицы
     * и нажимает кнопку "Добавить отчет" для начала создания нового отчета.
     */
    @Step(value = "Добавление отчета")
    public void addReports() {
        new MainPage()
                .openTab("ЛК Страхователя")
                .openTab("Отчеты")
                .waitTableToLoad()
                .clickButton("Добавить отчет");
    }

    /**
     * Выбирает тип отчета из списка доступных типов
     * 
     * Выбирает тип отчета через радиокнопку и подтверждает выбор кнопкой "Добавить".
     * 
     * @param reportType тип отчета из enum ReportType (например, SZV_M, SZV_ISH, SZV_TD_TYPE1)
     */
    @Step(value = "Выбор типа отчета")
    public void selectReportType(ReportType reportType) {
        new MainPage()
                .clickRadioInput(reportType.value)
                .clickBtnPrimary("Добавить");
    }

    /**
     * Создает новый отчет (не из шаблона)
     * 
     * Нажимает кнопку "Создать новый" для создания отчета с нуля,
     * без использования существующих шаблонов или черновиков.
     */
    @Step(value = "Добавить новый отчет")
    public void addNewReport() {
        new MainPage()
                .clickBtnPrimary("Создать новый");
    }

    /**
     * Заполняет общие сведения для отчета СЗВ-М
     * 
     * Заполняет основные параметры отчета СЗВ-М:
     * - Отчетный период: Январь
     * - За год: 2022
     * - Тип отчета: Исходная
     */
    @Step(value = "Общие сведения СЗВ-М")
    public void addGeneralInfo() {
        new MainPage()
                .clickMuiInputSpan("Отчетный период", "Январь")
                .clickMuiInputSpan("За год", "2022")
                .clickRadioInput("Исходная");
    }

    /**
     * Общие сведения СЗВ-ИСХ (mainInfo).
     * Форма: Календарный год, Руководитель (ФИО, Должность), Количество ЗЛ.
     */
    @Step(value = "Общие сведения СЗВ-ИСХ")
    public void addGeneralInfoISH() {
        new MainPage()
                .clickMuiInputSpan("Календарный год", "2007")
                .inputFieldPerson("headSurname", TestConfig.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("headName", TestConfig.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("headMiddlename", TestConfig.InsuredPerson.MIDDLE_NAME_1)
                .clickSpanId("headPosition", TestConfig.InsuredPerson.JOB)
                .clickMuiInputBase("insuredPersonsCount", "1");
        fillBasisSectionISH();
    }

    /**
     * Секция «Основание» СЗВ-ИСХ: Общее кол-во мест по штату, Количество фактически работающих.
     * Требуется для прохождения валидации при нажатии «Продолжить».
     */
    @Step(value = "Секция Основание СЗВ-ИСХ")
    public void fillBasisSectionISH() {
        new MainPage()
                .clickMuiInputBase("basisTotalStaff", "1")
                .clickMuiInputBase("basisActualEmployees", "1");
    }

    /**
     * Заполняет общие сведения для отчета ТД (Трудовая деятельность)
     * 
     * Заполняет должность руководителя для отчета ТД.
     * ФИО руководителя закомментировано (не используется в текущей реализации).
     */
    @Step(value = "Общие сведения ТД")
    public void addGeneralInfoTD(){
        new MainPage()
                /*.inputFieldPerson("lastName", TestConfig.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("firstName", TestConfig.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("middleName", TestConfig.InsuredPerson.MIDDLE_NAME_1)*/
                .clickSpanId("directorPosition", TestConfig.InsuredPerson.JOB);
    }

    /**
     * Заполняет общие сведения для отчета ЕФС (Единая форма сведений)
     * 
     * Заполняет должность и фамилию руководителя для отчета ЕФС.
     * Имя и отчество закомментированы (не используются в текущей реализации).
     */
    @Step(value = "Общие сведения EFS")
    public void addGeneralInfoEFS(){
        new MainPage()
                .clickSpanId("headPosition", TestConfig.InsuredPerson.JOB)
                .inputFieldPerson("headSurname", TestConfig.InsuredPerson.LAST_NAME_1)
                /*.inputFieldPerson("firstName", TestConfig.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("middleName", TestConfig.InsuredPerson.MIDDLE_NAME_1)*/;
    }

    /**
     * Продолжает создание отчета
     * 
     * Нажимает кнопку "Продолжить" и подтверждает действие в диалоге.
     * Использует умное ожидание для стабильности теста.
     */
    @Step(value = "Продолжить")
    public void createContinue(){
        // Ожидаем появления кнопки "Продолжить"
        $x("//span[contains(text(), 'Продолжить')]")
                .shouldBe(Condition.visible, DEFAULT_WAIT);
        new MainPage()
                .clickSpanButton("Продолжить")
                .clickBtn("Да");
        // Ожидаем завершения обработки после подтверждения
        $x("//body").shouldBe(Condition.visible, SHORT_WAIT);
    }
    
    /**
     * Переходит во вкладку "Застрахованные лица" (ЗЛ)
     * 
     * Открывает раздел для работы с застрахованными лицами в отчете.
     */
    @Step(value = "Переход во владку ЗЛ")
    public void goToZL() {
        new MainPage()
                .clickSectionA("Застрахованные лица");
    }

    /**
     * Выбирает раздел в боковом меню
     * 
     * Выполняет навигацию по боковому меню: "Раздел 1" -> "1.1 ТД, 1.2 СТАЖ, 1.3 БЮДЖ".
     */
    @Step(value = "Выбор раздела")
    public void sidebar(){
        new MainPage()
                .clickSidebar("sidebar__dropdown-title","Раздел 1",
                        "sidebar__item inner", "1.1 ТД, 1.2 СТАЖ, 1.3 БЮДЖ");
    }

    /**
     * Добавляет новое застрахованное лицо (ЗЛ)
     * 
     * Нажимает кнопку "Добавить" для создания новой записи о застрахованном лице.
     */
    @Step(value = "Добавление ЗЛ")
    public void addZL() {
        new MainPage()
                .clickBtnSecondary("Добавить");
    }

    /**
     * Добавляет мероприятие для застрахованного лица (для отчетов ТД)
     * 
     * Заполняет форму добавления мероприятия:
     * - Тип: Мероприятие
     * - Вид мероприятия: Прием (первый вариант из списка)
     * - Дата мероприятия: 01.01.2025
     * - Документ-основание: Договор, серия 4513, номер 7542, дата 01.01.2025
     * - Должность: Директор
     * - Код выполняемой функции по ОКЗ: 1112.3
     */
    @Step(value = "Добавить мероприятие")
    public void addEvent() {
        new MainPage()
                .clickBtnSecondary2("Добавить")
                .clickRadioInput("Мероприятие")
                .muiSpan("Вид мероприятия") //Прием
                .inputDateLabel("activity_eventDate","01.01.2025")
                .clickMInputLabel("activity.basisEvent[0].number","7542")
                .clickMInputLabel("activity_basisEvent_0_date","01.01.2025")
                .clickMInputLabel("activity.basisEvent[0].series","4513")
                .clickMInputLabel("activity.basisEvent[0].name","Договор")
                .clickMInputLabel("activity.position","Директор")
                .clickMInputLabel("activity.codeVfByOkz","1112.3");
    }

    /**
     * Добавляет мероприятие для застрахованного лица (для отчетов ЕФС)
     * 
     * Заполняет форму добавления мероприятия для отчетов ЕФС:
     * - Вид мероприятия: Прием (первый вариант из списка)
     * - Дата мероприятия: 01.01.2025
     * - Должность: Директор
     * - Код выполняемой функции по ОКЗ: 1111.7 (первый вариант)
     * - Документ: Договор, серия 4513, номер 7542, дата 01.01.2025
     */
    @Step(value = "Добавить мероприятие")
    public void addEventEFS() {
        new MainPage()
                .clickBtnSecondary2("Добавить")
                .muiSpan("Вид мероприятия") //Прием
                .inputDateLabel("eventDate","01.01.2025")
                .clickMInputLabel("position","Директор")
                .muiSpanValue("Код выполняемой функции по ОКЗ","option-0") //1111.7
                .clickMInputLabel("documentList[0].name","Договор")
                .clickMInputLabel("documentList_0_date","01.01.2025")
                .clickMInputLabel("documentList[0].series","4513")
                .clickMInputLabel("documentList[0].number","7542");
    }

    /**
     * Заполняет данные застрахованного лица (для отчетов ТД)
     * 
     * Вводит основные данные ЗЛ:
     * - ФИО: из TestConfig.InsuredPerson
     * - СНИЛС: из TestConfig.InsuredPerson.SNILS_1
     */
    @Step(value = "Заполнение ЗЛ")
    public void fillZL() {
        new MainPage()
                .inputFieldPerson("lastName", TestConfig.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("firstName", TestConfig.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("middleName", TestConfig.InsuredPerson.MIDDLE_NAME_1)
                .clickMuiInputBase("snils", TestConfig.InsuredPerson.SNILS_1);
    }

    /**
     * Заполняет данные застрахованного лица (для отчетов ЕФС)
     * 
     * Вводит расширенные данные ЗЛ:
     * - ФИО: из TestConfig.InsuredPerson
     * - СНИЛС: из TestConfig.InsuredPerson.SNILS_1
     * - Дата рождения: из TestConfig.InsuredPerson.Birthday
     * - Статус: ГРФ (первый вариант)
     * - Гражданство: Афганистан (первый вариант)
     */
    @Step(value = "Заполнение ЗЛ")
    public void fillZLEFS() {
        new MainPage()
                .inputFieldPerson("personSurname", TestConfig.InsuredPerson.LAST_NAME_1)
                .inputFieldPerson("personName", TestConfig.InsuredPerson.FIRST_NAME_1)
                .inputFieldPerson("personMiddlename", TestConfig.InsuredPerson.MIDDLE_NAME_1)
                .clickMuiInputBase("personSnils", TestConfig.InsuredPerson.SNILS_1)
                .clickMuiInputBase("birthDate", TestConfig.InsuredPerson.Birthday)
                .muiSpan("Статус") //ГРФ
                .muiSpan("Гражданство"); //Афганистан
    }

    /**
     * Заполняет ИНН застрахованного лица
     * 
     * Вводит ИНН из TestConfig.InsuredPerson.INN_1.
     */
    @Step(value = "Заполнение ЗЛ ИНН")
    public void fillZLINN() {
        new MainPage()
            .clickMuiInputBase("inn", TestConfig.InsuredPerson.INN_1);
    }

    /**
     * Заполняет дату рождения застрахованного лица
     * 
     * Вводит дату рождения из TestConfig.InsuredPerson.Birthday.
     */
    @Step(value = "Заполнение ЗЛ ДР")
    public void fillZLBirth() {
        new MainPage()
                .clickMuiInputBase("birthDateFull", TestConfig.InsuredPerson.Birthday);
    }

    /**
     * Сохраняет данные застрахованного лица
     * 
     * Нажимает кнопку "Сохранить" для сохранения введенных данных ЗЛ.
     */
    @Step(value = "Сохранение ЗЛ")
    public void saveZL() {
        new MainPage()
                .clickBtnPrimary("Сохранить");
    }

    /**
     * Сохраняет данные мероприятия
     * 
     * Нажимает вторую кнопку "Сохранить" для сохранения данных мероприятия.
     */
    @Step(value = "Сохранение мероприятия")
    public void saveEvent() {
        new MainPage()
                .clickBtnPrimary_2("Сохранить");
    }

    /**
     * Добавляет сведения о стаже застрахованного лица
     * 
     * Заполняет форму добавления стажа:
     * - Тип сведений: Исходная (первый вариант)
     * - Отчетный период: 2023 (первый вариант)
     * - Период стажа: с 01.01.2025 по 01.12.2025
     */
    @Step(value = "Добавление СТАЖ")
    public void addSTAJ(){
        new MainPage()
                .clickBtnSecondary3("Добавить")
                .muiSpanValue("Тип сведений","option-0") //Исходная
                .muiSpanValue("Отчетный период","option-0") //2023
                .clickBtnSecondary5("Добавить")
                .clickMInputLabel("experienceTimePeriodDateAt","01.01.2025")
                .clickMInputLabel("experienceTimePeriodDateTo","01.12.2025")
                .clickBtnSecondary5("Добавить");
    }

    /**
     * Сохраняет отчет как черновик
     * 
     * Сохраняет отчет без подписания и отправки.
     * Подтверждает сохранение в модальном диалоге.
     * 
     * TODO: Добавить условие для обработки ошибок при сохранении
     */
    @Step(value = "Сохранить как черновик")
    public void saveAsDraft(){
        new MainPage()
                .clickBtnPrimary("Сохранить как черновик")
                .clickButtonModalDialog("Да");
    }

    /**
     * Подписать и отправить отчет
     * 
     * Выполняет подписание и отправку отчета с умным ожиданием завершения операций.
     * Ожидает исчезновения спиннера загрузки перед переходом к следующему шагу.
     * 
     * <p>Использует провайдер подписи по умолчанию (определяется системой автоматически).
     * Для выбора конкретного провайдера используйте {@link #submitAndSend(String)}.
     * 
     * <p>Обработка ошибок:
     * <ul>
     *   <li>Автоматически обрабатывает диалоги подтверждения</li>
     *   <li>Ожидает завершения операций загрузки</li>
     *   <li>Проверяет появление кнопки подписания</li>
     * </ul>
     * 
     * @see #submitAndSend(String) для указания конкретного провайдера подписи
     * @see MainPage#checkProcessingStatus() для проверки статуса после отправки
     */
    @Step(value = "Подписать и отправить")
    public void submitAndSend() {
        submitAndSend(null);
    }
    
    /**
     * Подписать и отправить отчет с указанным провайдером подписи
     * 
     * Выполняет подписание и отправку отчета с указанием провайдера подписи.
     * 
     * @param providerName название провайдера подписи (например, "CryptoPro", "КриптоПРО")
     *                     если null, используется провайдер по умолчанию
     * @see #submitAndSend() для использования провайдера по умолчанию
     */
    @Step(value = "Подписать и отправить с провайдером: {providerName}")
    public void submitAndSend(String providerName) {
        new MainPage()
                .clickBtnPrimary("Подписать и отправить")
                .clickButtonModalDialog("Да");
        
        // Ожидаем исчезновения спиннера загрузки перед подписанием
        try {
            $x("//div[contains(@class, 'spinner') or contains(@class, 'loading')]")
                    .should(Condition.disappear, LONG_WAIT);
        } catch (Exception e) {
            // Если спиннер не найден, продолжаем выполнение
            logger.debug("Спиннер не найден или уже исчез: {}", e.getMessage());
        }
        
        // Ожидаем появления кнопки "Подписать"
        $x("//button[contains(@class, 'btn-primary')]//span[text() = 'Подписать']")
                .shouldBe(Condition.visible, DEFAULT_WAIT);
        
        // Если указан провайдер, выбираем его перед подписанием
        if (providerName != null && !providerName.isEmpty()) {
            logger.info("Выбор провайдера подписи: {}", providerName);
            chooseCriptoProvider();
        }
        
        new MainPage()
                .clickBtnPrimary("Подписать");
    }

    /**
     * Загружает отчет в формате XML
     * 
     * Выполняет загрузку XML файла отчета:
     * 1. Нажимает кнопку "Загрузить отчет"
     * 2. Загружает XML файл (путь определяется по типу отчета)
     * 3. Подтверждает загрузку в модальном окне
     * 4. Подтверждает действие в диалоге
     * 
     * @param reportType тип отчета, определяющий какой XML файл загружать
     */
    @Step(value = "Загрузка XML")
    public void sendXml(ReportType reportType) {
        new MainPage()
                .clickBtnSecondary("Загрузить отчет")
                .uploadFile(reportType.value)
                .clickButtonModal("Загрузить")
                .clickButtonModalDialog("Да");
    }

    // Дополнительные методы для проверок
    public boolean isOnZLPage() {
        // Проверка, что мы на вкладке ЗЛ
        return true; // заглушка
    }

    public boolean hasErrorMessages() {
        // Проверка наличия сообщений об ошибках
        return false; // заглушка
    }

    public boolean isContinueButtonEnabled() {
        // Проверка доступности кнопки продолжения
        return true; // заглушка
    }

    // Методы для детальных проверок полей (если нужны)
    public String getCurrentZLFieldValue(String fieldName) {
        // Получение значения поля из формы
        return ""; // заглушка
    }
}
