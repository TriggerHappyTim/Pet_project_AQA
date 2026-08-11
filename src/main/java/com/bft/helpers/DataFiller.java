package com.bft.helpers;

import com.bft.helpers.forms.FormElements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.asserts.SoftAssert;

/**
 * Хелпер для заполнения форм данными
 * 
 * Предоставляет методы для заполнения данных организации, отчетных данных,
 * добавления застрахованных лиц и управления формой (сохранение, редактирование, отправка).
 * 
 * <p>Пример использования:
 * <pre>{@code
 * SoftAssert softAssert = new SoftAssert();
 * DataFiller filler = new DataFiller(driver, softAssert);
 * 
 * // Заполнение данных организации
 * filler.fillInsurerData("ООО Ромашка", "123456", "7701234567", "770101001");
 * 
 * // Заполнение общих данных
 * filler.fillCommonData("2024-01", "0");
 * 
 * // Добавление застрахованного лица
 * InsuredPerson person = filler.addInsuredPerson(
 *     "Иванов", "Иван", "Иванович", "123-456-789 00", "770123456789"
 * );
 * 
 * // Сохранение формы
 * filler.saveForm();
 * 
 * softAssert.assertAll();
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 1.0
 * @see FormElements базовый класс для работы с элементами форм
 * @see InsuredPerson модель застрахованного лица
 * @since 1.0
 */
public class DataFiller extends FormElements {

    /**
     * Конструктор
     * 
     * @param driver экземпляр WebDriver
     * @param softAssert экземпляр SoftAssert для мягкой валидации
     */
    public DataFiller(WebDriver driver, SoftAssert softAssert) {
        super(driver, softAssert);
    }

    /**
     * Заполняет все данные организации
     * 
     * @param orgName наименование организации
     * @param pfrRegNumber регистрационный номер в ПФР
     * @param inn ИНН организации
     * @param kpp КПП организации
     */
    public void fillInsurerData(String orgName, String pfrRegNumber, String inn, String kpp) {
        fillField(getOrgNameField(), orgName, "Наименование организации");
        fillField(getPfrRegNumberField(), pfrRegNumber, "Рег. номер в ПФР");
        fillField(getInnField(), inn, "ИНН");
        fillField(getKppField(), kpp, "КПП");
    }

    /**
     * Заполняет наименование организации
     * 
     * @param orgName наименование организации
     */
    public void fillOrgName(String orgName) {
        fillField(getOrgNameField(), orgName, "Наименование организации");
    }

    /**
     * Заполняет регистрационный номер в ПФР
     * 
     * @param pfrRegNumber регистрационный номер в ПФР
     */
    public void fillPfrRegNumber(String pfrRegNumber) {
        fillField(getPfrRegNumberField(), pfrRegNumber, "Рег. номер в ПФР");
    }

    /**
     * Заполняет ИНН организации
     * 
     * @param inn ИНН организации
     */
    public void fillInn(String inn) {
        fillField(getInnField(), inn, "ИНН");
    }

    /**
     * Заполняет КПП организации
     * 
     * @param kpp КПП организации
     */
    public void fillKpp(String kpp) {
        fillField(getKppField(), kpp, "КПП");
    }

    /**
     * Заполняет общие данные формы
     * 
     * @param reportPeriod отчетный период
     * @param correctionNumber номер корректировки
     */
    public void fillCommonData(String reportPeriod, String correctionNumber) {
        selectReportPeriod(reportPeriod);
        fillCorrectionNumber(correctionNumber);
    }

    /**
     * Выбирает отчетный период из выпадающего списка
     * 
     * @param reportPeriod отчетный период
     */
    public void selectReportPeriod(String reportPeriod) {
        selectByVisibleText(getReportPeriodSelect(), reportPeriod, "Отчетный период");
    }

    /**
     * Заполняет номер корректировки
     * 
     * @param correctionNumber номер корректировки
     */
    public void fillCorrectionNumber(String correctionNumber) {
        fillField(getCorrectionNumberField(), correctionNumber, "Номер корректировки");
    }

    /**
     * Выбирает тип сведений из выпадающего списка
     * 
     * @param infoType тип сведений
     */
    public void selectInfoType(String infoType) {
        selectByVisibleText(getInfoTypeSelect(), infoType, "Тип сведений");
    }

    /**
     * Добавляет застрахованное лицо
     * 
     * Открывает вкладку застрахованных лиц, добавляет нового сотрудника
     * и заполняет его данные.
     * 
     * @param lastName фамилия
     * @param firstName имя
     * @param middleName отчество
     * @param snils СНИЛС
     * @param inn ИНН (может быть null)
     * @return объект InsuredPerson с данными добавленного лица
     */
    public com.bft.helpers.forms.InsuredPerson addInsuredPerson(String lastName, String firstName, String middleName,
                                                     String snils, String inn) {
        clickElement(getInsuredPersonsTab(), "Вкладка 'Застрахованные лица'");
        clickElement(getAddPersonButton(), "Добавить ЗЛ");

        WebElement lastNameField = driver.findElement(By.name("lastName"));
        WebElement firstNameField = driver.findElement(By.name("firstName"));
        WebElement middleNameField = driver.findElement(By.name("middleName"));
        WebElement snilsField = driver.findElement(By.name("snils"));
        WebElement innField = driver.findElement(By.name("personInn"));

        fillField(lastNameField, lastName, "Фамилия ЗЛ");
        fillField(firstNameField, firstName, "Имя ЗЛ");
        fillField(middleNameField, middleName, "Отчество ЗЛ");
        fillField(snilsField, snils, "СНИЛС ЗЛ");

        if (inn != null && !inn.isEmpty()) {
            fillField(innField, inn, "ИНН ЗЛ");
        }

        clickElement(driver.findElement(By.id("savePersonBtn")), "Сохранить ЗЛ");

        return new com.bft.helpers.forms.InsuredPerson(lastName, firstName, middleName, snils, inn);
    }

    /**
     * Открывает вкладку застрахованных лиц
     */
    public void openInsuredPersonsTab() {
        clickElement(getInsuredPersonsTab(), "Вкладка 'Застрахованные лица'");
    }

    /**
     * Сохраняет форму
     */
    public void saveForm() {
        clickElement(getSaveButton(), "Сохранить форму");
    }

    /**
     * Переводит форму в режим редактирования
     */
    public void editForm() {
        clickElement(getEditButton(), "Редактировать форму");
    }

    /**
     * Подписывает и отправляет форму
     */
    public void signAndSendForm() {
        clickElement(getSignAndSendButton(), "Подписать и отправить");
    }
}
