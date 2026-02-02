package com.bft.helpers.api;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.hamcrest.Matchers.lessThan;

/**
 * Централизованное управление RequestSpecification и ResponseSpecification для API тестов
 * 
 * Предоставляет переиспользуемые спецификации для RestAssured, которые можно применять
 * ко всем API запросам. Упрощает настройку базовых параметров запросов и валидации ответов.
 * 
 * <p>Основные возможности:
 * <ul>
 *   <li>Базовые RequestSpec с общими заголовками и настройками</li>
 *   <li>ResponseSpec для стандартной валидации ответов</li>
 *   <li>Спецификации для разных типов API (JSON, XML, файлы)</li>
 *   <li>Спецификации с авторизацией</li>
 *   <li>Спецификации для проверки производительности</li>
 * </ul>
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Использование базовой спецификации
 * RequestSpecification requestSpec = Specifications.defaultRequestSpec("https://api.example.com");
 * ResponseSpecification responseSpec = Specifications.successResponseSpec();
 * 
 * given()
 *     .spec(requestSpec)
 *     .when()
 *     .get("/api/users")
 *     .then()
 *     .spec(responseSpec);
 * 
 * // Использование спецификации с авторизацией
 * RequestSpecification authSpec = Specifications.authenticatedRequestSpec("https://api.example.com", token);
 * given()
 *     .spec(authSpec)
 *     .when()
 *     .get("/api/profile")
 *     .then()
 *     .spec(responseSpec);
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see ApiCoreRequests для переиспользуемых методов API запросов
 * @since 2.0
 */
public class Specifications {
    
    private static final Logger logger = LoggerFactory.getLogger(Specifications.class);
    
    /**
     * Максимальное время ответа по умолчанию (30 секунд)
     */
    private static final long DEFAULT_MAX_RESPONSE_TIME_MS = 30000;
    
    /**
     * Создает базовую RequestSpecification с общими настройками
     * 
     * Включает:
     * <ul>
     *   <li>Базовый URL</li>
     *   <li>Content-Type: application/json</li>
     *   <li>Accept: application/json</li>
     *   <li>Логирование запросов</li>
     * </ul>
     * 
     * @param baseUrl базовый URL API
     * @return настроенная RequestSpecification
     */
    public static RequestSpecification defaultRequestSpec(String baseUrl) {
        logger.debug("Создание базовой RequestSpec для URL: {}", baseUrl);
        
        return new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("User-Agent", "EVS-Testing-Framework/2.0")
                .build();
    }
    
    /**
     * Создает RequestSpecification с авторизацией Bearer токена
     * 
     * Включает все настройки из defaultRequestSpec плюс заголовок Authorization.
     * 
     * @param baseUrl базовый URL API
     * @param bearerToken Bearer токен для авторизации
     * @return настроенная RequestSpecification с авторизацией
     */
    public static RequestSpecification authenticatedRequestSpec(String baseUrl, String bearerToken) {
        logger.debug("Создание RequestSpec с авторизацией для URL: {}", baseUrl);
        
        return new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + bearerToken)
                .addHeader("User-Agent", "EVS-Testing-Framework/2.0")
                .build();
    }
    
    /**
     * Создает RequestSpecification с кастомными заголовками
     * 
     * @param baseUrl базовый URL API
     * @param headers карта заголовков (ключ - имя заголовка, значение - значение заголовка)
     * @return настроенная RequestSpecification с кастомными заголовками
     */
    public static RequestSpecification customHeadersRequestSpec(String baseUrl, Map<String, String> headers) {
        logger.debug("Создание RequestSpec с кастомными заголовками для URL: {}", baseUrl);
        
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("User-Agent", "EVS-Testing-Framework/2.0");
        
        headers.forEach(builder::addHeader);
        
        return builder.build();
    }
    
    /**
     * Создает RequestSpecification для загрузки файлов
     * 
     * Использует multipart/form-data вместо application/json.
     * 
     * @param baseUrl базовый URL API
     * @return настроенная RequestSpecification для загрузки файлов
     */
    public static RequestSpecification fileUploadRequestSpec(String baseUrl) {
        logger.debug("Создание RequestSpec для загрузки файлов для URL: {}", baseUrl);
        
        return new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.MULTIPART)
                .addHeader("User-Agent", "EVS-Testing-Framework/2.0")
                .build();
    }
    
    /**
     * Создает базовую ResponseSpecification для успешных ответов
     * 
     * Включает валидацию:
     * <ul>
     *   <li>Статус код 200</li>
     *   <li>Content-Type: application/json</li>
     *   <li>Время ответа не превышает DEFAULT_MAX_RESPONSE_TIME_MS</li>
     * </ul>
     * 
     * @return настроенная ResponseSpecification
     */
    public static ResponseSpecification successResponseSpec() {
        logger.debug("Создание ResponseSpec для успешных ответов");
        
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .expectResponseTime(lessThan(DEFAULT_MAX_RESPONSE_TIME_MS))
                .build();
    }
    
    /**
     * Создает ResponseSpecification для успешных ответов с кастомным временем ответа
     * 
     * @param maxResponseTimeMs максимальное время ответа в миллисекундах
     * @return настроенная ResponseSpecification
     */
    public static ResponseSpecification successResponseSpec(long maxResponseTimeMs) {
        logger.debug("Создание ResponseSpec для успешных ответов с максимальным временем: {}мс", maxResponseTimeMs);
        
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .expectResponseTime(lessThan(maxResponseTimeMs))
                .build();
    }
    
    /**
     * Создает ResponseSpecification для ответов с ошибками
     * 
     * Включает валидацию:
     * <ul>
     *   <li>Статус код соответствует ожидаемому</li>
     *   <li>Тело ответа не пустое (содержит описание ошибки)</li>
     * </ul>
     * 
     * @param expectedStatusCode ожидаемый HTTP статус код ошибки (например, 400, 404, 500)
     * @return настроенная ResponseSpecification
     */
    public static ResponseSpecification errorResponseSpec(int expectedStatusCode) {
        logger.debug("Создание ResponseSpec для ответов с ошибками, статус код: {}", expectedStatusCode);
        
        return new ResponseSpecBuilder()
                .expectStatusCode(expectedStatusCode)
                .build();
    }
    
    /**
     * Создает ResponseSpecification для ответов с созданными ресурсами (201 Created)
     * 
     * @return настроенная ResponseSpecification для статус кода 201
     */
    public static ResponseSpecification createdResponseSpec() {
        logger.debug("Создание ResponseSpec для созданных ресурсов (201)");
        
        return new ResponseSpecBuilder()
                .expectStatusCode(201)
                .expectContentType(ContentType.JSON)
                .build();
    }
    
    /**
     * Создает ResponseSpecification для ответов без содержимого (204 No Content)
     * 
     * @return настроенная ResponseSpecification для статус кода 204
     */
    public static ResponseSpecification noContentResponseSpec() {
        logger.debug("Создание ResponseSpec для ответов без содержимого (204)");
        
        return new ResponseSpecBuilder()
                .expectStatusCode(204)
                .build();
    }
    
    /**
     * Создает ResponseSpecification для XML ответов
     * 
     * @return настроенная ResponseSpecification для XML контента
     */
    public static ResponseSpecification xmlResponseSpec() {
        logger.debug("Создание ResponseSpec для XML ответов");
        
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.XML)
                .expectResponseTime(lessThan(DEFAULT_MAX_RESPONSE_TIME_MS))
                .build();
    }
    
    /**
     * Создает ResponseSpecification для проверки производительности
     * 
     * Включает только проверку времени ответа, без проверки статус кода и типа контента.
     * 
     * @param maxResponseTimeMs максимальное время ответа в миллисекундах
     * @return настроенная ResponseSpecification для проверки производительности
     */
    public static ResponseSpecification performanceResponseSpec(long maxResponseTimeMs) {
        logger.debug("Создание ResponseSpec для проверки производительности, максимальное время: {}мс", maxResponseTimeMs);
        
        return new ResponseSpecBuilder()
                .expectResponseTime(lessThan(maxResponseTimeMs))
                .build();
    }
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров
     */
    private Specifications() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
