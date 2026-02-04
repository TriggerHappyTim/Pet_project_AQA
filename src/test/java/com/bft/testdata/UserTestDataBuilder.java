package com.bft.testdata;

import com.bft.security.TestUsers;

/**
 * Builder для создания тестовых данных пользователей
 * 
 * <p>Предоставляет удобный способ создания объектов пользователей для тестов
 * с использованием Builder pattern и Faker для генерации реалистичных данных.
 * 
 * <p><b>Пример использования:</b>
 * <pre>{@code
 * // Создание пользователя с дефолтными значениями
 * UserData defaultUser = UserTestDataBuilder.createDefault().build();
 * 
 * // Создание пользователя с кастомными данными
 * UserData customUser = UserTestDataBuilder.createDefault()
 *     .withFirstName("Иван")
 *     .withLastName("Иванов")
 *     .withEmail("ivan@example.com")
 *     .withPhone("+7-999-123-45-67")
 *     .build();
 * 
 * // Создание случайного пользователя
 * UserData randomUser = UserTestDataBuilder.createRandom().build();
 * 
 * // Создание пользователя на основе TestUsers enum
 * UserData testUser = UserTestDataBuilder.fromTestUser(TestUsers.KRIVONOSOV_ALEXANDER).build();
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see TestUsers для предопределенных тестовых пользователей
 * @since 2.0
 */
public class UserTestDataBuilder extends TestDataBuilder<UserTestDataBuilder.UserData, UserTestDataBuilder> {
    
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String snils;
    private String organization;
    private String username;
    private String password;
    
    /**
     * Создает новый builder с дефолтными значениями
     * 
     * @return новый экземпляр UserTestDataBuilder
     */
    public static UserTestDataBuilder createDefault() {
        return new UserTestDataBuilder()
            .withFirstName("Иван")
            .withLastName("Иванов")
            .withEmail("ivan@example.com")
            .withPhone("+7-999-123-45-67")
            .withSnils("123-456-789-01")
            .withOrganization("Тестовая организация");
    }
    
    /**
     * Создает новый builder со случайными значениями
     * 
     * @return новый экземпляр UserTestDataBuilder со случайными данными
     */
    public static UserTestDataBuilder createRandom() {
        UserTestDataBuilder builder = new UserTestDataBuilder();
        return builder
            .withFirstName(faker.name().firstName())
            .withLastName(faker.name().lastName())
            .withEmail(builder.randomEmail())
            .withPhone(builder.randomPhone())
            .withSnils(generateSnils())
            .withOrganization(faker.company().name());
    }
    
    /**
     * Создает builder на основе предопределенного тестового пользователя
     * 
     * @param testUser предопределенный тестовый пользователь из TestUsers enum
     * @return новый экземпляр UserTestDataBuilder с данными из TestUsers
     */
    public static UserTestDataBuilder fromTestUser(TestUsers testUser) {
        var credentials = testUser.getCredentials();
        return new UserTestDataBuilder()
            .withUsername(credentials.username)
            .withPassword(credentials.password)
            .withEmail(credentials.email);
    }
    
    /**
     * Устанавливает имя пользователя
     * 
     * @param firstName имя пользователя
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public UserTestDataBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return self();
    }
    
    /**
     * Устанавливает фамилию пользователя
     * 
     * @param lastName фамилия пользователя
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public UserTestDataBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return self();
    }
    
    /**
     * Устанавливает email пользователя
     * 
     * @param email email адрес
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public UserTestDataBuilder withEmail(String email) {
        this.email = email;
        return self();
    }
    
    /**
     * Устанавливает телефонный номер пользователя
     * 
     * @param phone телефонный номер
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public UserTestDataBuilder withPhone(String phone) {
        this.phone = phone;
        return self();
    }
    
    /**
     * Устанавливает СНИЛС пользователя
     * 
     * @param snils СНИЛС в формате XXX-XXX-XXX-XX
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public UserTestDataBuilder withSnils(String snils) {
        this.snils = snils;
        return self();
    }
    
    /**
     * Устанавливает организацию пользователя
     * 
     * @param organization название организации
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public UserTestDataBuilder withOrganization(String organization) {
        this.organization = organization;
        return self();
    }
    
    /**
     * Устанавливает логин пользователя
     * 
     * @param username логин пользователя
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public UserTestDataBuilder withUsername(String username) {
        this.username = username;
        return self();
    }
    
    /**
     * Устанавливает пароль пользователя
     * 
     * @param password пароль пользователя
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public UserTestDataBuilder withPassword(String password) {
        this.password = password;
        return self();
    }
    
    /**
     * Строит объект UserData на основе настроек builder
     * 
     * @return объект UserData с установленными значениями
     * @throws IllegalArgumentException если данные не прошли валидацию
     */
    @Override
    public UserData build() {
        // Валидация данных перед созданием объекта
        validateUserData();
        
        return new UserData(firstName, lastName, email, phone, snils, organization, username, password);
    }
    
    /**
     * Валидирует данные пользователя перед созданием объекта
     * 
     * @throws IllegalArgumentException если данные не прошли валидацию
     */
    private void validateUserData() {
        // Валидация email
        if (email != null && !email.isEmpty()) {
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException("Некорректный формат email: " + email);
            }
        }
        
        // Валидация телефона
        if (phone != null && !phone.isEmpty()) {
            if (!isValidPhone(phone)) {
                throw new IllegalArgumentException("Некорректный формат телефона: " + phone);
            }
        }
        
        // Валидация СНИЛС
        if (snils != null && !snils.isEmpty()) {
            if (!isValidSnils(snils)) {
                throw new IllegalArgumentException("Некорректный формат СНИЛС: " + snils);
            }
        }
        
        // Валидация обязательных полей
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email является обязательным полем");
        }
    }
    
    /**
     * Проверяет валидность email адреса
     * 
     * @param email email адрес для проверки
     * @return true если email валиден, false в противном случае
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // Простая проверка формата email
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Проверяет валидность телефонного номера
     * 
     * @param phone телефонный номер для проверки
     * @return true если телефон валиден, false в противном случае
     */
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        // Проверка формата российского телефона (+7, 8, или без префикса)
        // Поддерживает форматы: +7-XXX-XXX-XX-XX, 8-XXX-XXX-XX-XX, XXX-XXX-XX-XX
        String phoneRegex = "^(\\+?7|8)?[-\\s]?\\d{3}[-\\s]?\\d{3}[-\\s]?\\d{2}[-\\s]?\\d{2}$";
        // Убираем все нецифровые символы для проверки длины
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        return digitsOnly.length() >= 10 && digitsOnly.length() <= 11;
    }
    
    /**
     * Проверяет валидность СНИЛС
     * 
     * @param snils СНИЛС для проверки в формате XXX-XXX-XXX-XX
     * @return true если СНИЛС валиден, false в противном случае
     */
    private boolean isValidSnils(String snils) {
        if (snils == null || snils.isEmpty()) {
            return false;
        }
        // Проверка формата СНИЛС: XXX-XXX-XXX-XX
        String snilsRegex = "^\\d{3}-\\d{3}-\\d{3}-\\d{2}$";
        if (!snils.matches(snilsRegex)) {
            return false;
        }
        // Убираем дефисы для проверки контрольной суммы
        String digitsOnly = snils.replaceAll("-", "");
        if (digitsOnly.length() != 11) {
            return false;
        }
        // Проверка контрольной суммы СНИЛС
        return validateSnilsChecksum(digitsOnly);
    }
    
    /**
     * Проверяет контрольную сумму СНИЛС
     * 
     * @param snilsDigits СНИЛС без дефисов (11 цифр)
     * @return true если контрольная сумма верна, false в противном случае
     */
    private boolean validateSnilsChecksum(String snilsDigits) {
        try {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                int digit = Character.getNumericValue(snilsDigits.charAt(i));
                sum += digit * (9 - i);
            }
            int checksum = sum % 101;
            if (checksum == 100) {
                checksum = 0;
            }
            int actualChecksum = Integer.parseInt(snilsDigits.substring(9));
            return checksum == actualChecksum;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Генерирует случайный СНИЛС в формате XXX-XXX-XXX-XX
     * 
     * @return случайный СНИЛС
     */
    private static String generateSnils() {
        return String.format("%03d-%03d-%03d-%02d",
            faker.number().numberBetween(100, 999),
            faker.number().numberBetween(100, 999),
            faker.number().numberBetween(100, 999),
            faker.number().numberBetween(10, 99)
        );
    }
    
    /**
     * Класс данных для хранения информации о пользователе
     */
    public static class UserData {
        private final String firstName;
        private final String lastName;
        private final String email;
        private final String phone;
        private final String snils;
        private final String organization;
        private final String username;
        private final String password;
        
        public UserData(String firstName, String lastName, String email, String phone,
                       String snils, String organization, String username, String password) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.snils = snils;
            this.organization = organization;
            this.username = username;
            this.password = password;
        }
        
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getSnils() { return snils; }
        public String getOrganization() { return organization; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        
        @Override
        public String toString() {
            return String.format("UserData{firstName='%s', lastName='%s', email='%s', organization='%s'}",
                firstName, lastName, email, organization);
        }
    }
}
