package com.bft.testdata;

/**
 * Константы тестовых данных для форм отчётности.
 *
 * <p>Вынесены из {@code com.bft.helpers.TestConfig}: единый источник констант
 * страхователя, застрахованных лиц и селекторов полей формы.
 */
public final class TestDataConstants {

    private TestDataConstants() {
    }

    // Данные страхователя
    public static final String ORG_NAME = "ООО 'Тестовая Компания'";
    public static final String ORG_SHORT_NAME = "Тестовая Компания";
    public static final String PFR_REG_NUMBER = "123-456-789012";
    public static final String INN = "1234567890";
    public static final String KPP = "123456789";
    public static final String REPORT_PERIOD = "Январь 2024";
    public static final String CORRECTION_NUMBER = "0";
    public static final String INFO_TYPE = "Исходные";

    // Данные застрахованных лиц
    public static class InsuredPerson {
        // Первое застрахованное лицо
        public static final String LAST_NAME_1 = "Иванов";
        public static final String FIRST_NAME_1 = "Иван";
        public static final String MIDDLE_NAME_1 = "Иванович";
        public static final String JOB = "Директор";
        public static final String SNILS_1 = "65869076870";
        public static final String INN_1 = "683434270877";
        public static final String Birthday = "01.01.1999";

        // Второе застрахованное лицо
        public static final String LAST_NAME_2 = "Петрова";
        public static final String FIRST_NAME_2 = "Мария";
        public static final String MIDDLE_NAME_2 = "Сергеевна";
        public static final String SNILS_2 = "987-654-321 09";
        public static final String INN_2 = "";

        // Создание объекта застрахованного лица
        public static com.bft.helpers.forms.InsuredPerson createPerson1() {
            return new com.bft.helpers.forms.InsuredPerson(LAST_NAME_1, FIRST_NAME_1, MIDDLE_NAME_1, SNILS_1, INN_1);
        }

        public static com.bft.helpers.forms.InsuredPerson createPerson2() {
            return new com.bft.helpers.forms.InsuredPerson(LAST_NAME_2, FIRST_NAME_2, MIDDLE_NAME_2, SNILS_2, INN_2);
        }
    }

    // Селекторы
    public static class Selectors {
        public static final String ORG_NAME_FIELD = "orgName";
        public static final String PFR_REG_NUMBER_FIELD = "pfrRegNumber";
        public static final String INN_FIELD = "inn";
        public static final String KPP_FIELD = "kpp";
        public static final String FILLING_DATE_FIELD = "fillingDate";
        public static final String REPORT_PERIOD_SELECT = "reportPeriod";
        public static final String CORRECTION_NUMBER_FIELD = "correctionNumber";
        public static final String INFO_TYPE_SELECT = "infoType";
        public static final String INSURED_PERSONS_TAB = "//a[contains(text(),'Застрахованные лица')]";
        public static final String ADD_PERSON_BUTTON = "addPersonBtn";
        public static final String SAVE_BUTTON = "saveBtn";
        public static final String EDIT_BUTTON = "editBtn";
        public static final String SIGN_AND_SEND_BUTTON = "signAndSendBtn";
    }
}