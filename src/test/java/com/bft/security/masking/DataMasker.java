package com.bft.security.masking;

/**
 * Интерфейс для маскировки чувствительных данных
 * Определяет контракты для различных стратегий маскировки
 */
public interface DataMasker {

    /**
     * Замаскировать чувствительные данные в строке
     * @param input входная строка
     * @return строка с замаскированными данными
     */
    String mask(String input);

    /**
     * Проверить, содержит ли строка чувствительные данные
     * @param input входная строка
     * @return true если содержит чувствительные данные
     */
    boolean containsSensitiveData(String input);

    /**
     * Получить тип маскировки
     */
    MaskerType getType();

    /**
     * Типы маскировки данных
     */
    enum MaskerType {
        PASSWORD("Маскировка паролей"),
        EMAIL("Маскировка email адресов"),
        CREDIT_CARD("Маскировка номеров карт"),
        SSN("Маскировка социальных номеров"),
        API_KEY("Маскировка API ключей"),
        JWT_TOKEN("Маскировка JWT токенов"),
        GENERIC("Общая маскировка");

        private final String description;

        MaskerType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}