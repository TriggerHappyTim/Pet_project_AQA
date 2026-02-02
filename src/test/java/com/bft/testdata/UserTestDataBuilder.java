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
public class UserTestDataBuilder extends TestDataBuilder<UserData, UserTestDataBuilder> {
    
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
        return new UserTestDataBuilder()
            .withFirstName(faker.name().firstName())
            .withLastName(faker.name().lastName())
            .withEmail(randomEmail())
            .withPhone(randomPhone())
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
            .withUsername(credentials != null ? credentials.username : null)
            .withPassword(credentials != null ? credentials.password : null)
            .withOrganization(credentials != null ? credentials.organization : null);
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
     */
    @Override
    public UserData build() {
        return new UserData(firstName, lastName, email, phone, snils, organization, username, password);
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
