package com.bft.helpers.forms;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.asserts.SoftAssert;

/**
 * Базовый класс для работы с элементами форм
 * 
 * Предоставляет общие методы для заполнения полей, выбора значений из выпадающих списков
 * и кликов по элементам с валидацией через SoftAssert.
 * 
 * @author QA Automation Team
 * @version 1.0
 * @since 1.0
 */
public class FormElements {
    
    protected final WebDriver driver;
    protected final SoftAssert softAssert;
    
    /**
     * Конструктор
     * 
     * @param driver экземпляр WebDriver
     * @param softAssert экземпляр SoftAssert для мягкой валидации
     */
    public FormElements(WebDriver driver, SoftAssert softAssert) {
        this.driver = driver;
        this.softAssert = softAssert;
    }
    
    // ==================== Локаторы полей организации ====================
    
    protected WebElement getOrgNameField() {
        return driver.findElement(By.name("orgName"));
    }
    
    protected WebElement getPfrRegNumberField() {
        return driver.findElement(By.name("pfrRegNumber"));
    }
    
    protected WebElement getInnField() {
        return driver.findElement(By.name("inn"));
    }
    
    protected WebElement getKppField() {
        return driver.findElement(By.name("kpp"));
    }
    
    // ==================== Локаторы общих полей ====================
    
    protected WebElement getReportPeriodSelect() {
        return driver.findElement(By.name("reportPeriod"));
    }
    
    protected WebElement getCorrectionNumberField() {
        return driver.findElement(By.name("correctionNumber"));
    }
    
    protected WebElement getInfoTypeSelect() {
        return driver.findElement(By.name("infoType"));
    }
    
    // ==================== Локаторы застрахованных лиц ====================
    
    protected WebElement getInsuredPersonsTab() {
        return driver.findElement(By.id("insuredPersonsTab"));
    }
    
    protected WebElement getAddPersonButton() {
        return driver.findElement(By.id("addPersonBtn"));
    }
    
    // ==================== Локаторы кнопок действий ====================
    
    protected WebElement getSaveButton() {
        return driver.findElement(By.id("saveBtn"));
    }
    
    protected WebElement getEditButton() {
        return driver.findElement(By.id("editBtn"));
    }
    
    protected WebElement getSignAndSendButton() {
        return driver.findElement(By.id("signAndSendBtn"));
    }
    
    // ==================== Методы заполнения полей ====================
    
    /**
     * Заполняет текстовое поле значением
     * 
     * @param element WebElement для заполнения
     * @param value значение для заполнения
     * @param fieldName имя поля для сообщения об ошибке
     */
    protected void fillField(WebElement element, String value, String fieldName) {
        try {
            element.clear();
            element.sendKeys(value);
            softAssert.assertTrue(true, fieldName + " успешно заполнено");
        } catch (Exception e) {
            softAssert.fail("Не удалось заполнить поле '" + fieldName + "': " + e.getMessage());
        }
    }
    
    /**
     * Выбирает значение из выпадающего списка по видимому тексту
     * 
     * @param selectElement WebElement выпадающего списка
     * @param visibleText видимый текст для выбора
     * @param fieldName имя поля для сообщения об ошибке
     */
    protected void selectByVisibleText(WebElement selectElement, String visibleText, String fieldName) {
        try {
            Select select = new Select(selectElement);
            select.selectByVisibleText(visibleText);
            softAssert.assertTrue(true, fieldName + " успешно выбран: " + visibleText);
        } catch (Exception e) {
            softAssert.fail("Не удалось выбрать значение '" + visibleText + "' в поле '" + fieldName + "': " + e.getMessage());
        }
    }
    
    /**
     * Кликает по элементу
     * 
     * @param element WebElement для клика
     * @param elementName имя элемента для сообщения об ошибке
     */
    protected void clickElement(WebElement element, String elementName) {
        try {
            element.click();
            softAssert.assertTrue(true, elementName + " успешно нажат");
        } catch (Exception e) {
            softAssert.fail("Не удалось нажать на элемент '" + elementName + "': " + e.getMessage());
        }
    }
}
