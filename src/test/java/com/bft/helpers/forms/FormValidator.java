package com.bft.helpers.forms;

import com.bft.security.masking.SecureLogger;
import com.bft.pw.ElementsCollection;
import com.bft.pw.SelenideElement;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.bft.pw.CollectionCondition.size;
import static com.bft.pw.CollectionCondition.sizeGreaterThan;
import static com.bft.pw.Condition.*;
import static com.bft.pw.Selenide.$;
import static com.bft.pw.Selenide.$$;

/**
 * Валидатор форм отчётности на базе Selenide.
 */
public class FormValidator {

    private static final Logger logger = (Logger) SecureLogger.getLogger(FormValidator.class);

    // === Селекторы (замените на актуальные ID/CSS из вашего приложения) ===
    private static final String ORG_NAME_ID = "orgName"; // Заменить на реальный ID
    private static final String PFR_REG_ID = "pfrRegNumber";
    private static final String INN_ID = "inn";
    private static final String KPP_ID = "kpp";
    private static final String FILLING_DATE_ID = "fillingDate";
    private static final String REPORT_PERIOD_ID = "reportPeriod";
    private static final String CORRECTION_NUMBER_ID = "correctionNumber";
    private static final String INFO_TYPE_ID = "infoType";
    private static final String INSURED_PERSONS_TAB_XPATH = "//a[contains(text(), 'ЗЛ')]"; // Пример
    private static final String ADD_PERSON_BUTTON_ID = "addPersonBtn";
    private static final String SAVE_BUTTON_ID = "saveBtn";
    private static final String EDIT_BUTTON_ID = "editBtn";
    private static final String SIGN_SEND_BUTTON_ID = "signSendBtn";

    // Селектор для строк таблицы застрахованных лиц
    private static final String PERSON_ROW_SELECTOR = "table tbody tr";

    /**
     * Проверка доступности полей блока страхователя в режиме создания.
     */
    public void verifyInsurerBlockInCreationMode() {
        logger.info("Проверка полей страхователя в режиме создания");

        $(ORG_NAME_ID).shouldBe(and("доступно и видно", visible, enabled));
        $(PFR_REG_ID).shouldBe(and("доступно и видно", visible, enabled));
        $(INN_ID).shouldBe(and("доступно и видно", visible, enabled));
        $(KPP_ID).shouldBe(and("доступно и видно", visible, enabled));
    }

    /**
     * Проверка данных ЗЛ в таблице.
     */
    public void verifyInsuredPersonData(InsuredPerson expectedPerson, int index) {
        logger.info("Проверка данных ЗЛ №{}: {} {} {}", index + 1, expectedPerson.getLastName(), expectedPerson.getFirstName(), expectedPerson.getMiddleName());

        ElementsCollection rows = $$(PERSON_ROW_SELECTOR);

        // Проверяем, что строк больше чем индекс
        rows.shouldHave(sizeGreaterThan(index));

        SelenideElement row = rows.get(index);
        String text = row.getText();

        row.shouldHave(text(expectedPerson.getLastName()));
        row.shouldHave(text(expectedPerson.getFirstName()));
        // Отчество может отсутствовать, поэтому проверяем мягче, если нужно
        if (expectedPerson.getMiddleName() != null && !expectedPerson.getMiddleName().isEmpty()) {
            row.shouldHave(text(expectedPerson.getMiddleName()));
        }
    }

    /**
     * Проверка количества ЗЛ в списке.
     */
    public void verifyInsuredPersonsCount(int expectedCount) {
        logger.info("Проверка количества ЗЛ: ожидаемое {}", expectedCount);
        $$(PERSON_ROW_SELECTOR).shouldHave(size(expectedCount));
    }

    /**
     * Проверка даты заполнения (должна быть текущей).
     */
    public void verifyFillingDateField() {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        logger.info("Проверка даты заполнения: ожидаемая {}", currentDate);

        $(FILLING_DATE_ID).shouldBe(visible).shouldHave(value(currentDate));
    }

    /**
     * Проверка полей в режиме редактирования.
     */
    public void verifyEditModeFields() {
        logger.info("Проверка полей в режиме редактирования");
        verifyCommonInfoBlock();
        verifyInfoTypeBlock();
        verifyInsurerBlockInCreationMode();
    }

    /**
     * Проверка блока общей информации.
     */
    public void verifyCommonInfoBlock() {
        $(REPORT_PERIOD_ID).shouldBe(and("доступно", visible, enabled));
        $(CORRECTION_NUMBER_ID).shouldBe(and("доступно", visible, enabled));
    }

    /**
     * Проверка блока типа сведений.
     */
    public void verifyInfoTypeBlock() {
        $(INFO_TYPE_ID).shouldBe(and("доступно", visible, enabled));
    }

    /**
     * Проверка режима чтения (поля заблокированы, кнопка активна).
     */
    public void verifyReadMode() {
        logger.info("Проверка режима чтения");

        // Кнопка должна быть активна
        $(SIGN_SEND_BUTTON_ID).shouldBe(and("активна", visible, enabled));

        // Поля должны быть видимы, но НЕ активны (disabled/readonly)
        $(ORG_NAME_ID).shouldBe(visible).shouldNotBe(enabled);
        $(PFR_REG_ID).shouldBe(visible).shouldNotBe(enabled);
        $(INN_ID).shouldBe(visible).shouldNotBe(enabled);
        $(KPP_ID).shouldBe(visible).shouldNotBe(enabled);
    }
}