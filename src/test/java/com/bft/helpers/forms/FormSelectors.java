package com.bft.helpers.forms;

/**
 * Централизованное хранилище селекторов для форм.
 */
public final class FormSelectors {

    // Поля общей информации
    public static final String ORG_NAME_FIELD = "#orgName, input[name='orgName']";
    public static final String PFR_REG_NUMBER_FIELD = "#pfrRegNumber, input[name='pfrRegNumber']";
    public static final String INN_FIELD = "#inn, input[name='inn']";
    public static final String KPP_FIELD = "#kpp, input[name='kpp']";
    public static final String FILLING_DATE_FIELD = "#fillingDate, input[name='fillingDate']";

    // Выпадающие списки
    public static final String REPORT_PERIOD_SELECT = "#reportPeriod, select[name='reportPeriod']";
    public static final String INFO_TYPE_SELECT = "#infoType, select[name='infoType']";

    // Поля коррекции
    public static final String CORRECTION_NUMBER_FIELD = "#correctionNumber, input[name='correctionNumber']";

    // Табы и кнопки
    public static final String INSURED_PERSONS_TAB = "//span[contains(text(), 'Застрахованные лица')]//ancestor::li | //a[contains(text(), 'ЗЛ')]";
    public static final String ADD_PERSON_BUTTON = "#addPersonBtn, button[contains(., 'Добавить')]";
    public static final String SAVE_BUTTON = "#saveBtn, button[contains(., 'Сохранить')]";
    public static final String EDIT_BUTTON = "#editBtn, button[contains(., 'Редактировать')]";
    public static final String SIGN_AND_SEND_BUTTON = "#signAndSendBtn, button[contains(., 'Подписать и отправить')]";

    private FormSelectors() {
        throw new UnsupportedOperationException("Utility class");
    }
}