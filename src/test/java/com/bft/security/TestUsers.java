package com.bft.security;

import com.bft.security.EnvironmentCredentialProvider.UserCredentials;

/**
 * Перечисление тестовых пользователей для авторизации в системе EVS
 * 
 * <p>Каждый пользователь содержит только идентификатор (credential prefix),
 * который используется для получения реальных учетных данных из:
 * <ul>
 *   <li>Переменных окружения (приоритет для CI/CD)</li>
 *   <li>Файла credentials.properties (для локальной разработки)</li>
 * </ul>
 * 
 * <p><b>Безопасность:</b> Пароли НЕ хранятся в коде, только ключи для поиска.
 * 
 * <p><b>Пример использования:</b>
 * <pre>
 * {@code
 * // В тесте
 * steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
 * 
 * // В credentials.properties
 * evs.user1.username=133-900-785 49
 * evs.user1.password=Egisso13?
 * evs.user1.organization=ОРГАНИЗАЦИЯ -1546025669
 * }
 * </pre>
 * 
 * @see CredentialManager
 * @see EnvironmentCredentialProvider
 */
public enum TestUsers {
    
    /**
     * Кривоносов Александр Петрович
     * Credential prefix: evs.user1
     * Организация: ОРГАНИЗАЦИЯ -1546025669
     */
    KRIVONOSOV_ALEXANDER("evs.user1", "Кривоносов Александр Петрович"),
    
    /**
     * Бездомный Иван Николаевич
     * Credential prefix: evs.user2
     * Организация: ОРГАНИЗАЦИЯ -2036470831
     */
    BEZDOMNIY_IVAN("evs.user2", "Бездомный Иван Николаевич"),
    
    /**
     * Пользователь по умолчанию (для обратной совместимости)
     * Credential prefix: evs
     * Организация: ОРГАНИЗАЦИЯ -1546025669
     */
    DEFAULT_USER("evs", "Тестовый пользователь по умолчанию");
    
    private final String credentialPrefix;
    private final String fullName;
    
    TestUsers(String credentialPrefix, String fullName) {
        this.credentialPrefix = credentialPrefix;
        this.fullName = fullName;
    }
    
    /**
     * Получить префикс для поиска credentials
     * 
     * @return префикс для ключей (например, "evs.user1")
     */
    public String getCredentialPrefix() {
        return credentialPrefix;
    }
    
    /**
     * Получить полное имя пользователя
     * 
     * @return ФИО пользователя
     */
    public String getFullName() {
        return fullName;
    }
    
    /**
     * Получить учетные данные пользователя через CredentialManager
     * 
     * <p>Ищет credentials по ключам:
     * <ul>
     *   <li>{prefix}.username</li>
     *   <li>{prefix}.password</li>
     *   <li>{prefix}.email (опционально)</li>
     * </ul>
     * 
     * @return объект UserCredentials с данными пользователя
     * @throws IllegalStateException если credentials не найдены или невалидны
     */
    public UserCredentials getCredentials() {
        CredentialManager cm = CredentialManager.getInstance();
        
        String username = cm.getCredential(credentialPrefix + ".username");
        String password = cm.getCredential(credentialPrefix + ".password");
        String email = cm.getCredential(credentialPrefix + ".email");
        
        if (username == null || password == null) {
            throw new IllegalStateException(
                String.format("Credentials для пользователя '%s' не найдены. " +
                    "Проверьте наличие ключей: %s.username и %s.password в credentials.properties или переменных окружения",
                    fullName, credentialPrefix, credentialPrefix)
            );
        }
        
        return new UserCredentials(username, password, email);
    }
    
    /**
     * Получить имя пользователя (логин)
     * 
     * @return username или null, если не найден
     */
    public String getUsername() {
        return CredentialManager.getInstance()
            .getCredential(credentialPrefix + ".username");
    }
    
    /**
     * Получить пароль пользователя
     * 
     * @return password или null, если не найден
     */
    public String getPassword() {
        return CredentialManager.getInstance()
            .getCredential(credentialPrefix + ".password");
    }
    
    /**
     * Получить название организации для выбора после авторизации
     * 
     * @return название организации (например, "ОРГАНИЗАЦИЯ -1546025669")
     */
    public String getOrganization() {
        String org = CredentialManager.getInstance()
            .getCredential(credentialPrefix + ".organization");
        
        if (org == null) {
            throw new IllegalStateException(
                String.format("Организация для пользователя '%s' не найдена. " +
                    "Проверьте наличие ключа: %s.organization в credentials.properties",
                    fullName, credentialPrefix)
            );
        }
        
        return org;
    }
    
    /**
     * Проверить доступность учетных данных
     * 
     * @return true, если все обязательные credentials доступны
     */
    public boolean hasCredentials() {
        CredentialManager cm = CredentialManager.getInstance();
        return cm.hasCredential(credentialPrefix + ".username") 
            && cm.hasCredential(credentialPrefix + ".password")
            && cm.hasCredential(credentialPrefix + ".organization");
    }
    
    /**
     * Получить полное имя пользователя с логином
     * 
     * @return строка вида "Иванов Иван Иванович (ivan@mail.ru)"
     */
    @Override
    public String toString() {
        String username = getUsername();
        if (username != null) {
            return fullName + " (" + username + ")";
        }
        return fullName;
    }
}
