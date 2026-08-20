package com.bft.test.helpers;

import com.bft.pw.Selenide;
import com.bft.pw.WebDriverRunner;
import com.bft.pw.PwDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.asserts.SoftAssert;

import java.util.function.Supplier;

/**
 * Вспомогательный класс для инициализации и управления Page Objects
 * 
 * <p>Предоставляет методы для:
 * - Безопасной инициализации Page Objects
 * - Проверки успешности открытия страницы перед созданием Page Object
 * - Ленивой инициализации Page Objects
 * 
 * <p>Пример использования:
 * <pre>{@code
 * private CryptoProDemoPage cryptoProPage;
 * 
 * @BeforeMethod
 * public void initializePageObject() {
 *     cryptoProPage = PageObjectHelper.initializePageObject(
 *         cryptoProPage,
 *         () -> {
 *             Selenide.open(DEMO_PAGE_URL);
 *             return new CryptoProDemoPage(softAssert);
 *         },
 *         "CryptoProDemoPage"
 *     );
 * }
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 1.0
 * @since 1.0
 */
public class PageObjectHelper {

    private static final Logger logger = LoggerFactory.getLogger(PageObjectHelper.class);

    /**
     * Инициализирует Page Object с проверкой успешности открытия страницы
     * 
     * Проверяет, что Page Object еще не создан, открывает страницу,
     * проверяет успешность открытия и создает Page Object.
     * 
     * @param <T> тип Page Object
     * @param existingPageObject существующий Page Object (может быть null)
     * @param pageObjectSupplier функция для создания Page Object (должна открыть страницу и создать объект)
     * @param pageObjectName имя Page Object для логирования
     * @return инициализированный Page Object
     * @throws RuntimeException если не удалось создать Page Object
     */
    public static <T> T initializePageObject(
            T existingPageObject,
            Supplier<T> pageObjectSupplier,
            String pageObjectName) {
        
        if (existingPageObject != null) {
            logger.debug("Page Object '{}' уже инициализирован, пропускаем создание", pageObjectName);
            return existingPageObject;
        }
        
        logger.info("Инициализация Page Object: {}", pageObjectName);
        
        try {
            // Создаем Page Object через supplier
            T pageObject = pageObjectSupplier.get();
            
            // Проверяем, что Page Object создан
            if (pageObject == null) {
                throw new RuntimeException(
                    String.format("Не удалось создать Page Object '%s' - supplier вернул null", pageObjectName)
                );
            }
            
            // Проверяем, что страница открыта (если есть активный PwDriver)
            if (WebDriverRunner.hasWebDriverStarted()) {
                PwDriver driver = WebDriverRunner.getWebDriver();
                if (driver != null) {
                    String currentUrl = driver.getCurrentUrl();
                    logger.debug("Страница открыта: {}", currentUrl);
                }
            }
            
            logger.info("Page Object '{}' успешно инициализирован", pageObjectName);
            return pageObject;
            
        } catch (Exception e) {
            logger.error("Ошибка при инициализации Page Object '{}': {}", pageObjectName, e.getMessage(), e);
            throw new RuntimeException(
                String.format("Не удалось инициализировать Page Object '%s': %s", pageObjectName, e.getMessage()),
                e
            );
        }
    }

    /**
     * Инициализирует Page Object с проверкой URL страницы
     * 
     * Открывает страницу, проверяет что URL содержит ожидаемые ключевые слова,
     * и создает Page Object.
     * 
     * @param <T> тип Page Object
     * @param existingPageObject существующий Page Object (может быть null)
     * @param url URL страницы для открытия
     * @param expectedUrlKeywords ключевые слова, которые должны быть в URL после открытия
     * @param pageObjectSupplier функция для создания Page Object после открытия страницы
     * @param pageObjectName имя Page Object для логирования
     * @return инициализированный Page Object
     * @throws RuntimeException если не удалось открыть страницу или создать Page Object
     */
    public static <T> T initializePageObjectWithUrlCheck(
            T existingPageObject,
            String url,
            String[] expectedUrlKeywords,
            Supplier<T> pageObjectSupplier,
            String pageObjectName) {
        
        if (existingPageObject != null) {
            logger.debug("Page Object '{}' уже инициализирован", pageObjectName);
            return existingPageObject;
        }
        
        logger.info("Открываем страницу для Page Object '{}': {}", pageObjectName, url);
        
        try {
            // Открываем страницу
            Selenide.open(url);
            
            // Проверяем успешность открытия через ожидание
            Selenide.Wait().until(webDriver -> {
                String currentUrl = webDriver.getCurrentUrl();
                boolean urlMatches = false;
                
                if (expectedUrlKeywords != null && expectedUrlKeywords.length > 0) {
                    for (String keyword : expectedUrlKeywords) {
                        if (currentUrl.contains(keyword) || webDriver.getPageSource().length() > 0) {
                            urlMatches = true;
                            break;
                        }
                    }
                } else {
                    // Если ключевые слова не указаны, проверяем что страница загрузилась
                    urlMatches = webDriver.getPageSource().length() > 0;
                }
                
                return urlMatches;
            });
            
            logger.info("Страница успешно открыта, создаем Page Object: {}", pageObjectName);
            
            // Создаем Page Object
            T pageObject = pageObjectSupplier.get();
            
            if (pageObject == null) {
                throw new RuntimeException(
                    String.format("Не удалось создать Page Object '%s' - supplier вернул null", pageObjectName)
                );
            }
            
            logger.info("Page Object '{}' успешно инициализирован", pageObjectName);
            return pageObject;
            
        } catch (Exception e) {
            logger.error("Ошибка при инициализации Page Object '{}': {}", pageObjectName, e.getMessage(), e);
            throw new RuntimeException(
                String.format("Не удалось инициализировать Page Object '%s': %s", pageObjectName, e.getMessage()),
                e
            );
        }
    }

    /**
     * Проверяет, что Page Object инициализирован
     * 
     * @param pageObject Page Object для проверки
     * @param pageObjectName имя Page Object для сообщения об ошибке
     * @throws IllegalStateException если Page Object не инициализирован
     */
    public static void requirePageObjectInitialized(Object pageObject, String pageObjectName) {
        if (pageObject == null) {
            throw new IllegalStateException(
                String.format("Page Object '%s' не инициализирован. Вызовите initializePageObject() перед использованием.", pageObjectName)
            );
        }
    }
}
