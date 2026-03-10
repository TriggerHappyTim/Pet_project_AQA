package com.bft.constants;

/**
 * Константы URL для тестов EVS Testing Framework
 * 
 * Централизованное хранение всех URL, используемых в тестах.
 * Использование констант вместо хардкода URL улучшает поддержку
 * и позволяет менять URL в одном месте.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * import static com.bft.constants.UrlConstants.*;
 * 
 * Selenide.open(CRYPTOPRO_DEMO_PAGE_URL);
 * }</pre>
 * 
 * @author QA Automation Team
 * @since 2.0
 */
public final class UrlConstants {

    // ========== КриптоПРО ==========

    /**
     * URL демо-страницы КриптоПРО для проверки электронной подписи
     */
    public static final String CRYPTOPRO_DEMO_PAGE_URL =
            "https://cryptopro.ru/sites/default/files/products/cades/demopage/cades_bes_sample.html";

    /**
     * Базовый URL сайта КриптоПРО
     */
    public static final String CRYPTOPRO_BASE_URL = "https://cryptopro.ru";

    /**
     * URL страницы продуктов КриптоПРО
     */
    public static final String CRYPTOPRO_PRODUCTS_URL = "https://cryptopro.ru/products";

    // ========== API ==========

    /**
     * Базовый URL API по умолчанию
     */
    public static final String API_BASE_URL_DEFAULT = "https://api.example.com";

    private UrlConstants() {
    }
}
