package com.bft.security;

/**
 * Провайдер учетных данных на основе переменных окружения
 * 
 * Безопасно получает credentials из системных переменных окружения и system properties.
 * Приоритет: system properties > environment variables.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * EnvironmentCredentialProvider provider = new EnvironmentCredentialProvider();
 * 
 * // Получение отдельного credential
 * String apiKey = provider.getCredential("api.key");
 * 
 * // Получение пользовательских credentials с префиксом
 * UserCredentials evs = provider.getUserCredentials("evs");
 * // Ищет: evs.username, evs.password, evs.email
 * 
 * // Получение API credentials
 * ApiCredentials api = provider.getApiCredentials("service");
 * // Ищет: service.key, service.secret, service.token
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see CredentialProvider базовый интерфейс
 * @see CredentialManager для централизованного управления credentials
 */
public class EnvironmentCredentialProvider implements CredentialProvider {

    /**
     * Получает credential по ключу из system properties или environment variables
     * 
     * Порядок поиска:
     * 1. System.getProperty(key) - приоритет выше
     * 2. System.getenv(key) - если не найдено в properties
     * 
     * @param key ключ для поиска credential (например, "evs.username")
     * @return значение credential или null если не найдено
     */
    @Override
    public String getCredential(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }

        // Получаем значение из системных свойств (приоритет выше)
        String systemProperty = System.getProperty(key);
        if (systemProperty != null) {
            return systemProperty;
        }

        // Получаем значение из переменных окружения
        return System.getenv(key);
    }

    @Override
    public boolean hasCredential(String key) {
        return getCredential(key) != null;
    }

    @Override
    public ProviderType getType() {
        return ProviderType.ENVIRONMENT;
    }

    /**
     * Получает credential с префиксом для группировки
     * 
     * Комбинирует префикс и ключ через точку для поиска группированных credentials.
     * Например: getCredentialWithPrefix("db", "username") ищет "db.username"
     * 
     * @param prefix префикс для группировки (например, "db", "evs", "api")
     * @param key ключ credential (например, "username", "password")
     * @return значение credential или null если не найдено
     */
    public String getCredentialWithPrefix(String prefix, String key) {
        String fullKey = prefix + "." + key;
        return getCredential(fullKey);
    }

    /**
     * Получает пользовательские credentials по префиксу
     * 
     * Ищет три ключа с заданным префиксом:
     * - {prefix}.username
     * - {prefix}.password
     * - {prefix}.email
     * 
     * <p>Пример:
     * <pre>{@code
     * // Переменные окружения:
     * // evs.username=user@test.com
     * // evs.password=secret123
     * // evs.email=user@test.com
     * 
     * UserCredentials creds = getUserCredentials("evs");
     * }</pre>
     * 
     * @param userPrefix префикс для поиска пользовательских credentials
     * @return объект UserCredentials с найденными данными (могут быть null внутри)
     */
    public UserCredentials getUserCredentials(String userPrefix) {
        return new UserCredentials(
            getCredentialWithPrefix(userPrefix, "username"),
            getCredentialWithPrefix(userPrefix, "password"),
            getCredentialWithPrefix(userPrefix, "email")
        );
    }

    /**
     * Получает API credentials по префиксу
     * 
     * Ищет три ключа с заданным префиксом:
     * - {prefix}.key - API ключ
     * - {prefix}.secret - секретный ключ
     * - {prefix}.token - токен авторизации
     * 
     * @param apiPrefix префикс для поиска API credentials (например, "github", "aws")
     * @return объект ApiCredentials с найденными данными (могут быть null внутри)
     */
    public ApiCredentials getApiCredentials(String apiPrefix) {
        return new ApiCredentials(
            getCredentialWithPrefix(apiPrefix, "key"),
            getCredentialWithPrefix(apiPrefix, "secret"),
            getCredentialWithPrefix(apiPrefix, "token")
        );
    }

    /**
     * Получает database credentials по префиксу
     * 
     * Ищет четыре ключа с заданным префиксом:
     * - {prefix}.url - JDBC URL подключения
     * - {prefix}.username - имя пользователя БД
     * - {prefix}.password - пароль пользователя БД
     * - {prefix}.driver - класс JDBC драйвера
     * 
     * @param dbPrefix префикс для поиска database credentials (например, "postgres", "mysql")
     * @return объект DatabaseCredentials с найденными данными (могут быть null внутри)
     */
    public DatabaseCredentials getDatabaseCredentials(String dbPrefix) {
        return new DatabaseCredentials(
            getCredentialWithPrefix(dbPrefix, "url"),
            getCredentialWithPrefix(dbPrefix, "username"),
            getCredentialWithPrefix(dbPrefix, "password"),
            getCredentialWithPrefix(dbPrefix, "driver")
        );
    }

    /**
     * Класс для хранения пользовательских учетных данных
     * 
     * Безопасное хранение credentials пользователя. Объект иммутабелен
     * после создания для обеспечения потокобезопасности.
     */
    public static class UserCredentials {
        public final String username;
        public final String password;
        public final String email;

        /**
         * Создает объект с пользовательскими учетными данными
         * 
         * @param username имя пользователя (логин или email)
         * @param password пароль пользователя
         * @param email email адрес пользователя
         */
        public UserCredentials(String username, String password, String email) {
            this.username = username;
            this.password = password;
            this.email = email;
        }

        /**
         * Проверяет валидность пользовательских credentials
         * 
         * Credentials считаются валидными если username и password
         * не null и не пустые строки (после trim).
         * 
         * @return true если credentials валидны, false в противном случае
         */
        public boolean isValid() {
            return username != null && !username.trim().isEmpty() &&
                   password != null && !password.trim().isEmpty();
        }

        @Override
        public String toString() {
            return "UserCredentials{username='***', password='***', email='" +
                   (email != null ? "***@" + email.substring(email.indexOf('@') + 1) : "null") + "'}";
        }
    }

    /**
     * Класс для хранения API учетных данных
     * 
     * Поддерживает два способа аутентификации:
     * - Token-based (только token)
     * - Key-Secret pair (key + secret)
     */
    public static class ApiCredentials {
        public final String key;
        public final String secret;
        public final String token;

        /**
         * Создает объект с API учетными данными
         * 
         * @param key API ключ (может быть null)
         * @param secret секретный ключ (может быть null)
         * @param token токен авторизации (может быть null)
         */
        public ApiCredentials(String key, String secret, String token) {
            this.key = key;
            this.secret = secret;
            this.token = token;
        }

        /**
         * Проверяет наличие валидного токена
         * 
         * @return true если token не null и не пустая строка
         */
        public boolean hasValidToken() {
            return token != null && !token.trim().isEmpty();
        }

        /**
         * Проверяет наличие валидной пары key-secret
         * 
         * @return true если key и secret не null и не пустые строки
         */
        public boolean hasValidKeySecret() {
            return key != null && !key.trim().isEmpty() &&
                   secret != null && !secret.trim().isEmpty();
        }

        @Override
        public String toString() {
            return "ApiCredentials{key='***', secret='***', token='" +
                   (token != null ? "***" + token.substring(Math.max(0, token.length() - 4)) : "null") + "'}";
        }
    }

    /**
     * Класс для хранения database учетных данных
     * 
     * Содержит все необходимые параметры для подключения к базе данных
     * через JDBC.
     */
    public static class DatabaseCredentials {
        public final String url;
        public final String username;
        public final String password;
        public final String driver;

        /**
         * Создает объект с database учетными данными
         * 
         * @param url JDBC URL подключения (например, "jdbc:postgresql://localhost:5432/mydb")
         * @param username имя пользователя БД
         * @param password пароль пользователя БД
         * @param driver полное имя класса JDBC драйвера (например, "org.postgresql.Driver")
         */
        public DatabaseCredentials(String url, String username, String password, String driver) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.driver = driver;
        }

        /**
         * Проверяет валидность database credentials
         * 
         * Credentials считаются валидными если url, username и password
         * не null и не пустые строки. Driver опционален.
         * 
         * @return true если credentials валидны, false в противном случае
         */
        public boolean isValid() {
            return url != null && !url.trim().isEmpty() &&
                   username != null && !username.trim().isEmpty() &&
                   password != null && !password.trim().isEmpty();
        }

        @Override
        public String toString() {
            return "DatabaseCredentials{url='" + url + "', username='***', password='***', driver='" + driver + "'}";
        }
    }
}