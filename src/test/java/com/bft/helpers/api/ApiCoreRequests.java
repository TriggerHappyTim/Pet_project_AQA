// ============================================================================
// ЗАКОММЕНТИРОВАНО: API тесты не используются в проекте
// ============================================================================
// package com.bft.helpers.api;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

import static io.restassured.RestAssured.given;

/**
 * Переиспользуемые методы для выполнения API запросов
 * 
 * Предоставляет высокоуровневые методы для выполнения стандартных HTTP операций
 * с автоматическим логированием, обработкой ошибок и retry механизмом.
 * 
 * <p>Основные возможности:
 * <ul>
 *   <li>GET, POST, PUT, DELETE, PATCH запросы</li>
 *   <li>Автоматическое применение RequestSpec и ResponseSpec</li>
 *   <li>Логирование всех запросов и ответов</li>
 *   <li>Retry механизм для нестабильных запросов</li>
 *   <li>Автоматическая обработка ошибок</li>
 * </ul>
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Использование со спецификациями
 * RequestSpecification requestSpec = Specifications.defaultRequestSpec("https://api.example.com");
 * ResponseSpecification responseSpec = Specifications.successResponseSpec();
 * 
 * Response response = ApiCoreRequests.get("/api/users/123", requestSpec, responseSpec);
 * 
 * // Использование с кастомной валидацией
 * Response response = ApiCoreRequests.post(
 *     "/api/users",
 *     userData,
 *     requestSpec,
 *     response -> {
 *         response.then().statusCode(201);
 *         // дополнительная валидация
 *     }
 * );
 * 
 * // Использование с retry
 * Response response = ApiCoreRequests.getWithRetry(
 *     "/api/users/123",
 *     requestSpec,
 *     responseSpec,
 *     3  // количество попыток
 * );
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see Specifications для создания RequestSpec и ResponseSpec
 * @since 2.0
 */
// public class ApiCoreRequests {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiCoreRequests.class);
    
    /**
     * Максимальное количество попыток retry по умолчанию
     */
    private static final int DEFAULT_MAX_RETRIES = 3;
    
    /**
     * Интервал между попытками retry в миллисекундах
     */
    private static final long RETRY_INTERVAL_MS = 1000;
    
    /**
     * Выполняет GET запрос
     * 
     * @param endpoint путь к API endpoint (например, "/api/users/123")
     * @param requestSpec RequestSpecification с настройками запроса
     * @param responseSpec ResponseSpecification для валидации ответа
     * @return объект Response с ответом от сервера
     */
    public static Response get(String endpoint, RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        logger.info("Выполнение GET запроса: {}", endpoint);
        
        Response response = given()
                .spec(requestSpec)
                .when()
                .get(endpoint)
                .then()
                .spec(responseSpec)
                .extract()
                .response();
        
        logResponse(response);
        return response;
    }
    
    /**
     * Выполняет GET запрос с кастомной валидацией
     * 
     * @param endpoint путь к API endpoint
     * @param requestSpec RequestSpecification с настройками запроса
     * @param validator функция для кастомной валидации ответа
     * @return объект Response с ответом от сервера
     */
    public static Response get(String endpoint, RequestSpecification requestSpec, Consumer<Response> validator) {
        logger.info("Выполнение GET запроса с кастомной валидацией: {}", endpoint);
        
        Response response = given()
                .spec(requestSpec)
                .when()
                .get(endpoint);
        
        validator.accept(response);
        logResponse(response);
        return response;
    }
    
    /**
     * Выполняет POST запрос
     * 
     * @param endpoint путь к API endpoint (например, "/api/users")
     * @param body тело запроса (будет сериализовано в JSON)
     * @param requestSpec RequestSpecification с настройками запроса
     * @param responseSpec ResponseSpecification для валидации ответа
     * @return объект Response с ответом от сервера
     */
    public static Response post(String endpoint, Object body, RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        logger.info("Выполнение POST запроса: {}", endpoint);
        logger.debug("Тело запроса: {}", body);
        
        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .spec(responseSpec)
                .extract()
                .response();
        
        logResponse(response);
        return response;
    }
    
    /**
     * Выполняет POST запрос с кастомной валидацией
     * 
     * @param endpoint путь к API endpoint
     * @param body тело запроса
     * @param requestSpec RequestSpecification с настройками запроса
     * @param validator функция для кастомной валидации ответа
     * @return объект Response с ответом от сервера
     */
    public static Response post(String endpoint, Object body, RequestSpecification requestSpec, Consumer<Response> validator) {
        logger.info("Выполнение POST запроса с кастомной валидацией: {}", endpoint);
        logger.debug("Тело запроса: {}", body);
        
        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post(endpoint);
        
        validator.accept(response);
        logResponse(response);
        return response;
    }
    
    /**
     * Выполняет PUT запрос
     * 
     * @param endpoint путь к API endpoint (например, "/api/users/123")
     * @param body тело запроса (будет сериализовано в JSON)
     * @param requestSpec RequestSpecification с настройками запроса
     * @param responseSpec ResponseSpecification для валидации ответа
     * @return объект Response с ответом от сервера
     */
    public static Response put(String endpoint, Object body, RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        logger.info("Выполнение PUT запроса: {}", endpoint);
        logger.debug("Тело запроса: {}", body);
        
        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .put(endpoint)
                .then()
                .spec(responseSpec)
                .extract()
                .response();
        
        logResponse(response);
        return response;
    }
    
    /**
     * Выполняет DELETE запрос
     * 
     * @param endpoint путь к API endpoint (например, "/api/users/123")
     * @param requestSpec RequestSpecification с настройками запроса
     * @param responseSpec ResponseSpecification для валидации ответа
     * @return объект Response с ответом от сервера
     */
    public static Response delete(String endpoint, RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        logger.info("Выполнение DELETE запроса: {}", endpoint);
        
        Response response = given()
                .spec(requestSpec)
                .when()
                .delete(endpoint)
                .then()
                .spec(responseSpec)
                .extract()
                .response();
        
        logResponse(response);
        return response;
    }
    
    /**
     * Выполняет PATCH запрос
     * 
     * @param endpoint путь к API endpoint (например, "/api/users/123")
     * @param body тело запроса (будет сериализовано в JSON)
     * @param requestSpec RequestSpecification с настройками запроса
     * @param responseSpec ResponseSpecification для валидации ответа
     * @return объект Response с ответом от сервера
     */
    public static Response patch(String endpoint, Object body, RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        logger.info("Выполнение PATCH запроса: {}", endpoint);
        logger.debug("Тело запроса: {}", body);
        
        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .patch(endpoint)
                .then()
                .spec(responseSpec)
                .extract()
                .response();
        
        logResponse(response);
        return response;
    }
    
    /**
     * Выполняет GET запрос с retry механизмом
     * 
     * Повторяет запрос указанное количество раз при ошибках.
     * 
     * @param endpoint путь к API endpoint
     * @param requestSpec RequestSpecification с настройками запроса
     * @param responseSpec ResponseSpecification для валидации ответа
     * @param maxRetries максимальное количество попыток
     * @return объект Response с ответом от сервера
     * @throws RuntimeException если все попытки исчерпаны
     */
    public static Response getWithRetry(String endpoint, RequestSpecification requestSpec, 
                                       ResponseSpecification responseSpec, int maxRetries) {
        logger.info("Выполнение GET запроса с retry ({} попыток): {}", maxRetries, endpoint);
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.debug("Попытка {} из {}", attempt, maxRetries);
                return get(endpoint, requestSpec, responseSpec);
            } catch (Exception e) {
                lastException = e;
                logger.warn("Попытка {} не удалась: {}", attempt, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        
        logger.error("Все {} попыток исчерпаны для GET {}", maxRetries, endpoint);
        throw new RuntimeException("Failed to execute GET request after " + maxRetries + " attempts", lastException);
    }
    
    /**
     * Выполняет POST запрос с retry механизмом
     * 
     * @param endpoint путь к API endpoint
     * @param body тело запроса
     * @param requestSpec RequestSpecification с настройками запроса
     * @param responseSpec ResponseSpecification для валидации ответа
     * @param maxRetries максимальное количество попыток
     * @return объект Response с ответом от сервера
     * @throws RuntimeException если все попытки исчерпаны
     */
    public static Response postWithRetry(String endpoint, Object body, RequestSpecification requestSpec,
                                       ResponseSpecification responseSpec, int maxRetries) {
        logger.info("Выполнение POST запроса с retry ({} попыток): {}", maxRetries, endpoint);
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.debug("Попытка {} из {}", attempt, maxRetries);
                return post(endpoint, body, requestSpec, responseSpec);
            } catch (Exception e) {
                lastException = e;
                logger.warn("Попытка {} не удалась: {}", attempt, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        
        logger.error("Все {} попыток исчерпаны для POST {}", maxRetries, endpoint);
        throw new RuntimeException("Failed to execute POST request after " + maxRetries + " attempts", lastException);
    }
    
    /**
     * Логирует детали ответа
     * 
     * @param response объект Response для логирования
     */
    private static void logResponse(Response response) {
        logger.debug("Ответ получен:");
        logger.debug("  Статус код: {}", response.getStatusCode());
        logger.debug("  Время ответа: {} мс", response.getTime());
        logger.debug("  Content-Type: {}", response.getContentType());
        
        if (response.getBody() != null) {
            try {
                String body = response.getBody().asString();
                if (body.length() > 500) {
                    logger.debug("  Тело ответа (первые 500 символов): {}", body.substring(0, 500) + "...");
                } else {
                    logger.debug("  Тело ответа: {}", body);
                }
            } catch (Exception e) {
                logger.debug("  Не удалось получить тело ответа: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров
     */
    private ApiCoreRequests() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
// }
