package com.bft.helpers.forms;

import com.bft.enums.TimeoutConstants;
import com.bft.pw.Condition;
import com.bft.pw.ElementsCollection;
import com.bft.pw.SelenideElement;
import com.bft.pw.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bft.pw.CollectionCondition.size;
import static com.bft.pw.Condition.enabled;
import static com.bft.pw.Condition.visible;
import static com.bft.pw.Selenide.$;
import static com.bft.pw.Selenide.$$;

/**
 * Фасад для работы с формами отчетов.
 * Инкапсулирует логику заполнения, сохранения и валидации.
 * Рефакторинг: переход на Selenide, удаление зависимостей от WebDriver/SoftAssert.
 */
public class FormFacade {

    private static final Logger logger = LoggerFactory.getLogger(FormFacade.class);

    // === Селекторы (можно вынести в отдельный класс FormSelectors) ===
    private static final String SAVE_BTN_XPATH = "//button[contains(@class, 'save') or contains(text(), 'Сохранить')]";
    private static final String EDIT_BTN_XPATH = "//button[contains(@class, 'edit') or contains(text(), 'Редактировать')]";
    private static final String TABLE_ROWS_XPATH = "//tbody[contains(@class, 'n2o-table-tbody')]//tr";

    /**
     * Заполняет данные страхователя и общие данные формы.
     */
    public FormFacade fillCommonReportData(String orgName, String regNumber, String inn, String kpp,
                                           String reportPeriod, String correctionNumber, String infoType) {
        logger.info("Заполнение данных страхователя и общих полей");

        // Пример заполнения (селекторы нужно адаптировать под реальную верстку)
        $("input[id*='orgName'], input[name*='orgName']").shouldBe(visible, TimeoutConstants.DEFAULT_WAIT).setValue(orgName);
        $("input[id*='regNumber'], input[name*='regNumber']").setValue(regNumber);
        $("input[id*='inn'], input[name*='inn']").setValue(inn);
        $("input[id*='kpp'], input[name*='kpp']").setValue(kpp);

        $("input[id*='fillingDate']").setValue(reportPeriod); // Или использовать календарь
        $("select[id*='correctionNumber']").selectOption(correctionNumber);
        $("select[id*='infoType']").selectOption(infoType);

        logger.info("Данные страхователя заполнены");
        return this;
    }

    /**
     * Добавляет застрахованное лицо и проверяет его появление в таблице.
     * Возвращает сам объект фасада для цепочки вызовов.
     */
    public FormFacade addInsuredPersonAndVerify(String lastName, String firstName, String middleName,
                                                String snils, String inn, int expectedIndex) {
        logger.info("Добавление застрахованного лица: {} {} {}", lastName, firstName, middleName);

        // 1. Нажать "Добавить"
        $(By.xpath("//button[contains(text(), 'Добавить') or contains(@class, 'add')]"))
                .shouldBe(enabled, TimeoutConstants.DEFAULT_WAIT).click();

        // 2. Заполнить модальное окно (селекторы примерные)
        $("input[id*='lastName']").setValue(lastName);
        $("input[id*='firstName']").setValue(firstName);
        $("input[id*='middleName']").setValue(middleName);
        $("input[id*='snils']").setValue(snils);
        $("input[id*='innPerson']").setValue(inn);

        // 3. Сохранить в модальном окне
        $(By.xpath("//button[contains(@class, 'modal-save') or contains(text(), 'Сохранить')]"))
                .shouldBe(enabled, TimeoutConstants.DEFAULT_WAIT).click();

        // 4. Проверка появления в таблице
        ElementsCollection rows = $$(By.xpath(TABLE_ROWS_XPATH));
        rows.shouldHave(size(expectedIndex), TimeoutConstants.LONG_WAIT);

        logger.info("Застрахованное лицо добавлено, всего записей в таблице: {}", expectedIndex);
        return this;
    }

    /**
     * Сохраняет форму и проверяет переход в режим чтения.
     */
    public FormFacade saveFormAndVerifyReadMode() {
        logger.info("Сохранение формы и проверка режима чтения");

        SelenideElement saveBtn = $(By.xpath(SAVE_BTN_XPATH));
        saveBtn.shouldBe(enabled, TimeoutConstants.DEFAULT_WAIT).scrollTo().click();

        // Ожидание исчезновения кнопки сохранения или появления сообщения об успехе
        try {
            saveBtn.should(Condition.disappear, TimeoutConstants.LONG_WAIT);
        } catch (Exception e) {
            logger.debug("Кнопка 'Сохранить' не исчезла, проверяем наличие сообщения об успехе...");
        }

        // Проверка, что кнопка "Редактировать" появилась (признак режима чтения)
        $(By.xpath(EDIT_BTN_XPATH)).shouldBe(visible, TimeoutConstants.DEFAULT_WAIT);

        logger.info("Форма сохранена, режим чтения подтвержден");
        return this;
    }

    /**
     * Переводит форму в режим редактирования.
     */
    public FormFacade editFormAndVerify() {
        logger.info("Переход в режим редактирования");

        $(By.xpath(EDIT_BTN_XPATH)).shouldBe(enabled, TimeoutConstants.DEFAULT_WAIT).click();

        // Проверка, что поля стали доступны для ввода
        $("input[id*='orgName']").shouldBe(enabled, TimeoutConstants.DEFAULT_WAIT);

        logger.info("Режим редактирования активирован");
        return this;
    }

    /**
     * Проверяет количество записей в таблице.
     */
    public FormFacade verifyTableSize(int expectedSize) {
        logger.info("Проверка количества записей в таблице: ожидаем {}", expectedSize);
        ElementsCollection rows = $$(By.xpath(TABLE_ROWS_XPATH));
        rows.shouldHave(size(expectedSize), TimeoutConstants.DEFAULT_WAIT);
        return this;
    }
}