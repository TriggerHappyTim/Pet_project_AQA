package com.bft.steps;

import com.bft.enums.TabType;
import com.bft.ui.component.TableComponent;
import com.bft.pw.Condition;
import com.bft.pw.ElementsCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import java.time.Duration;

import static com.bft.pw.Selenide.*;

/**
 * Класс шагов для валидации отчетов и данных.
 * Вынесен из MainPage для разделения ответственности (Actions vs Assertions).
 */
public class ReportValidationSteps {

    private static final Logger log = LoggerFactory.getLogger(ReportValidationSteps.class);

    // Компонент таблицы для переиспользования
    private final TableComponent resultsTable = TableComponent.createResultsTable("Таблица результатов");
    private final SoftAssert softAssert = new SoftAssert();

    /**
     * Проверяет положительный результат протокола проверки.
     */
    public ReportValidationSteps verifyProtocolPositive() {
        waitForUppStatus();

        boolean noErrors = $x("//div[@class='noDataTable']").isDisplayed();
        String uppStatus = $x("//div[contains(@class, 'chip_with-bg')]").getText();

        log.debug("Статус УПП: {}", uppStatus);

        Assert.assertEquals(uppStatus, "Положительный", "Статус УПП должен быть 'Положительный'");
        Assert.assertTrue(noErrors, "В протоколе не должно быть ошибок (таблица пуста)");

        return this;
    }

    /**
     * Проверяет наличие отчетов страхователя.
     */
    public ReportValidationSteps verifyInsurerReportsExist() {
        resultsTable.waitForLoad();

        ElementsCollection rows = $$x("//tbody[@class= 'n2o-advanced-table-tbody']/tr");
        Assert.assertNotEquals(rows.size(), 0, "Таблица отчетов страхователя пуста");

        rows.get(0).click();
        waitIlsDocumentLoad();

        // Возврат назад реализован через браузер, лучше вынести в NavigationSteps или делать явно в тесте
        back();
        return this;
    }

    /**
     * Проверяет наличие данных о стаже.
     */
    public ReportValidationSteps verifyStazhData() {
        resultsTable.waitForLoad();

        ElementsCollection stazhRows = $$x("(//tbody[@class= 'n2o-advanced-table-tbody'])[1]/tr");
        Assert.assertNotEquals(stazhRows.size(), 0, "Таблица стажа пуста");

        stazhRows.get(0).click();

        $x("(//div[@class = 'pgs-output-text'])[1]/*[2]")
                .shouldHave(Condition.exactText("ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ \"ФОРВАРД\""), Duration.ofSeconds(10));

        back();
        return this;
    }

    /**
     * Проверяет вкладку на отсутствие ошибок и пустоты.
     */
    public ReportValidationSteps verifyTabNotEmptyNoErrors(String tabName, TabType tabType) {
        // Предполагается, что навигация вызывается отдельно или через новый NavigationSteps
        // Если нужно оставить здесь, то потребуется зависимость от MainPage или NavigationComponent
        // Для чистоты архитектуры лучше вызывать openTab() из теста перед этим методом

        resultsTable.waitForLoad();

        String tableName = resultsTable.getTableTitle();
        boolean hasError = resultsTable.hasError();

        softAssert.assertFalse(hasError, "На вкладке '" + tableName + "' обнаружена ошибка");
        if (hasError) {
            log.debug("ОШИБКА на вкладке {}: {}", tableName, "Текст ошибки (если есть)");
            resultsTable.closeErrorAlert();
        }

        if (tabType == TabType.TABLE) {
            resultsTable.clickFirstRow();
        }

        boolean isNotEmpty = resultsTable.isNotEmpty();
        softAssert.assertTrue(isNotEmpty, "Таблица '" + tableName + "' пуста");

        int rowCount = resultsTable.getRowCount();
        if (!isNotEmpty) {
            log.debug("Количество записей в таблице {} = {}", tableName, rowCount);
        } else {
            log.debug("Успешно: количество записей в таблице {} = {}", tableName, rowCount);
        }

        return this;
    }

    /**
     * Проверяет базовую информацию о физическом лице (ФИО, ИНН).
     */
    public ReportValidationSteps verifyPersonBasicInfo(String expectedLastName, String expectedFirstName, String expectedSecondName) {
        resultsTable.waitForLoad();

        String lastName = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[1]").getText();
        String firstName = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[2]").getText();
        String secondName = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[3]").getText();
        String inn = $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[7]").getText();

        log.debug("ФИО: {} {} {}, ИНН: {}", lastName, firstName, secondName, inn);

        softAssert.assertEquals(lastName, expectedLastName, "Фамилия не совпадает");
        softAssert.assertEquals(firstName, expectedFirstName, "Имя не совпадает");
        softAssert.assertEquals(secondName, expectedSecondName, "Отчество не совпадает");

        return this;
    }

    // --- Приватные вспомогательные методы (бывшие из MainPage) ---

    private void waitForUppStatus() {
        $x("//div[contains(@class, 'chip_with-bg')]")
                .shouldNot(Condition.exactText("Данных нет"), Duration.ofSeconds(20));
    }

    private void waitIlsDocumentLoad() {
        $x("(//div[@class= 'n2o-output-text pgs-output-text__output left'])[3]")
                .shouldNotBe(Condition.exactText("ДАННЫХ НЕТ"), Duration.ofSeconds(10));
    }

    /**
     * Получает экземпляр SoftAssert для внешних проверок (если нужно).
     */
    public SoftAssert getSoftAssert() {
        return softAssert;
    }
}