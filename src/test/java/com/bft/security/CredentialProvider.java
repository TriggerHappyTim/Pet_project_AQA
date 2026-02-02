package com.bft.security;

/**
 * Интерфейс для безопасного получения учетных данных
 * Предоставляет абстракцию для различных источников credentials
 */
public interface CredentialProvider {

    /**
     * Получить учетные данные по ключу
     * @param key ключ учетных данных
     * @return учетные данные или null если не найдены
     */
    String getCredential(String key);

    /**
     * Получить учетные данные с дефолтным значением
     * @param key ключ учетных данных
     * @param defaultValue значение по умолчанию
     * @return учетные данные или defaultValue
     */
    default String getCredential(String key, String defaultValue) {
        String value = getCredential(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Проверить доступность учетных данных
     * @param key ключ учетных данных
     * @return true если учетные данные доступны
     */
    boolean hasCredential(String key);

    /**
     * Получить тип провайдера
     */
    ProviderType getType();

    /**
     * Типы провайдеров учетных данных
     */
    enum ProviderType {
        ENVIRONMENT("Переменные окружения"),
        PROPERTIES_FILE("Файл свойств"),
        SECURE_VAULT("Защищенное хранилище"),
        MOCK("Мок данные для тестирования");

        private final String description;

        ProviderType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}