package com.bft.constants;

/**
 * Константы URL для тестов EVS Testing Framework
 * 
 * Централизованное хранение всех URL адресов, используемых в тестах.
 * Использование констант вместо хардкода улучшает поддерживаемость
 * и позволяет легко изменять URL для разных окружений.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * import static com.bft.constants.UrlConstants.*;
 * 
 * // Вместо:
 * Selenide.open("https://cryptopro.ru/sites/default/files/products/cades/demopage/cades_bes_sample.html");
 * 
 * // Используйте:
 * Selenide.open(CRYPTOPRO_DEMO_PAGE_URL);
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @since 2.0
 */
public final class UrlConstants {
    
    // ========== КриптоПРО URLs ==========
    
    /**
     * URL демо-страницы КриптоПРО для тестирования электронной подписи
     * 
     * Используется для проверки работы плагина КриптоПРО в браузере.
     * Страница содержит примеры создания подписи и проверки сертификатов.
     */
    public static final String CRYPTOPRO_DEMO_PAGE_URL = 
            "https://cryptopro.ru/sites/default/files/products/cades/demopage/cades_bes_sample.html";
    
    /**
     * Базовый URL сайта КриптоПРО
     * 
     * Используется для добавления в список доверенных сайтов.
     */
    public static final String CRYPTOPRO_BASE_URL = "https://cryptopro.ru";
    
    /**
     * URL страницы продуктов КриптоПРО
     * 
     * Используется для добавления в список доверенных сайтов.
     */
    public static final String CRYPTOPRO_PRODUCTS_URL = 
            "https://cryptopro.ru/sites/default/files/products/cades/demopage/";
    
    // ========== API URLs (по умолчанию) ==========
    
    /**
     * Базовый URL API по умолчанию для локальной разработки
     * 
     * Используется как fallback значение, если не указан через переменные окружения.
     * В production должен быть переопределен через переменную окружения API_BASE_URL.
     */
    public static final String API_BASE_URL_DEFAULT = "http://localhost:8080";
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров
     * Этот класс содержит только статические константы
     */
    private UrlConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
