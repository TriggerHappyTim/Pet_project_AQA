// ============================================================================
// ЗАКОММЕНТИРОВАНО: API тесты не используются в проекте
// ============================================================================
// package com.bft.test.base;

import com.bft.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.asserts.SoftAssert;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.restassured.RestAssured.given;

/**
 * Базовый класс для API тестов с разделением ответственности
 * 
 * Предоставляет инфраструктуру для написания REST API тестов с использованием RestAssured.
 * Реализует паттерн Given-When-Then для структурирования API тестов.
 * 
 * <p>Основные возможности:
 * <ul>
 *   <li>Паттерн Given-When-Then для структурирования тестов</li>
 *   <li>Упрощенные методы для GET и POST запросов</li>
 *   <li>Стандартные валидаторы ответов (успех, JSON, ошибка, время ответа)</li>
 *   <li>Вспомогательные методы для создания запросов (авторизация, заголовки, параметры)</li>
 *   <li>Автоматическое логирование запросов и ответов</li>
 *   <li>Создание тестовых данных</li>
 * </ul>
 * 
 * <p>Пример использования:
 * <pre>{@code
 * public class UserApiTest extends ApiTestBase {
 *     
 *     @Test(groups = {"api", "smoke"})
 *     public void getUserById() {
 *         givenWhenThen(
 *             request -> request.baseUri(baseUrl).header("Authorization", "Bearer " + token),
 *             request -> request.when().get("/api/users/123"),
 *             response -> {
 *                 response.then()
 *                     .statusCode(200)
 *                     .body("id", equalTo(123))
 *                     .body("email", notNullValue());
 *             },
 *             "Get User by ID"
 *         );
 *     }
 *     
 *     @Test(groups = {"api"})
 *     public void createUser() {
 *         Map<String, Object> userData = Map.of("name", "Test User", "email", "test@example.com");
 *         postAndVerify("/api/users", userData, 201, validateSuccessResponse());
 *     }
 * }
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see BaseTest для базовой функциональности
 * @see UITestBase для UI тестов
 * @since 1.0
 */
// public abstract class ApiTestBase extends BaseTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // Базовые настройки API
    protected final String baseUrl = System.getProperty("api.base.url", "http://localhost:8080");
    protected final String apiVersion = System.getProperty("api.version", "v1");

    /**
     * Паттерн Given-When-Then для API тестов
     * 
     * Структурирует API тест на три этапа:
     * 1. Given - подготовка запроса (URL, заголовки, параметры)
     * 2. When - выполнение HTTP запроса
     * 3. Then - валидация ответа (статус код, тело ответа)
     * 
     * <p>Автоматически логирует каждый этап и обрабатывает ошибки.
     * 
     * @param given функция для подготовки RequestSpecification (настройка URL, заголовков, параметров)
     * @param when функция для выполнения HTTP запроса и получения Response
     * @param then функция для валидации Response (проверка статус кода, тела ответа)
     */
    protected void givenWhenThen(
            Function<RequestSpecification, RequestSpecification> given,
            Function<RequestSpecification, Response> when,
            Consumer<Response> then) {

        givenWhenThen(given, when, then, "API Test");
    }

    /**
     * Расширенный паттерн Given-When-Then с указанием имени теста
     * 
     * Структурирует API тест на три этапа с кастомным именем для логирования.
     * 
     * @param given функция для подготовки RequestSpecification (настройка URL, заголовков, параметров)
     * @param when функция для выполнения HTTP запроса и получения Response
     * @param then функция для валидации Response (проверка статус кода, тела ответа)
     * @param testName имя теста для логирования и отчетности
     */
    protected void givenWhenThen(
            Function<RequestSpecification, RequestSpecification> given,
            Function<RequestSpecification, Response> when,
            Consumer<Response> then,
            String testName) {

        logger.info("=== НАЧАЛО API ТЕСТА: {} ===", testName);

        Response response = null;

        try {
            // Given - подготовка запроса
            logger.info("📋 ШАГ 1: Подготовка запроса");
            RequestSpecification request = given.apply(given());

            // When - выполнение запроса
            logger.info("🎯 ШАГ 2: Выполнение запроса");
            response = when.apply(request);

            // Then - валидация ответа
            logger.info("✅ ШАГ 3: Валидация ответа");
            then.accept(response);

            logger.info("✅ API ТЕСТ ПРОЙДЕН: {}", testName);

        } catch (Exception e) {
            logger.error("❌ API ТЕСТ ПРОВАЛЕН: {} - {}", testName, e.getMessage());
            if (response != null) {
                logResponseDetails(response);
            }
            throw e;
        }
    }

    /**
     * Упрощенный паттерн для GET запросов
     * 
     * Выполняет GET запрос к указанному endpoint и валидирует ответ.
     * Использует базовый URL из конфигурации.
     * 
     * @param endpoint путь к API endpoint (например, "/api/users/123")
     * @param expectedStatus ожидаемый HTTP статус код (например, 200, 404)
     * @param validator функция для дополнительной валидации ответа (проверка тела ответа, заголовков)
     */
    protected void getAndVerify(String endpoint, int expectedStatus, Consumer<Response> validator) {
        givenWhenThen(
            request -> request.baseUri(baseUrl),
            request -> request.when().get(endpoint),
            response -> {
                response.then().statusCode(expectedStatus);
                validator.accept(response);
            },
            "GET " + endpoint
        );
    }

    /**
     * Упрощенный паттерн для POST запросов
     * 
     * Выполняет POST запрос с телом к указанному endpoint и валидирует ответ.
     * Автоматически устанавливает Content-Type: application/json.
     * 
     * @param endpoint путь к API endpoint (например, "/api/users")
     * @param body тело запроса (объект, который будет сериализован в JSON)
     * @param expectedStatus ожидаемый HTTP статус код (например, 201 для создания ресурса)
     * @param validator функция для дополнительной валидации ответа (проверка созданного ресурса)
     */
    protected void postAndVerify(String endpoint, Object body, int expectedStatus, Consumer<Response> validator) {
        givenWhenThen(
            request -> request
                .baseUri(baseUrl)
                .contentType("application/json"),
            request -> request
                .body(body)
                .when()
                .post(endpoint),
            response -> {
                response.then().statusCode(expectedStatus);
                validator.accept(response);
            },
            "POST " + endpoint
        );
    }

    /**
     * Валидатор для успешных ответов (статус код 2xx)
     * 
     * Проверяет, что статус код находится в диапазоне 200-299
     * и тело ответа не пустое.
     * 
     * @return Consumer для валидации успешного ответа
     */
    protected Consumer<Response> validateSuccessResponse() {
        return response -> {
            softAssert.assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300,
                "Ожидался успешный статус код (2xx), получен: " + response.getStatusCode());
            softAssert.assertNotNull(response.getBody(),
                "Тело ответа не должно быть пустым");
        };
    }

    /**
     * Валидатор для JSON ответов
     * 
     * Проверяет, что ответ имеет Content-Type: application/json,
     * статус код 200 и содержит валидный JSON.
     * 
     * @return Consumer для валидации JSON ответа
     */
    protected Consumer<Response> validateJsonResponse() {
        return response -> {
            response.then()
                .contentType("application/json")
                .statusCode(200);
            softAssert.assertNotNull(response.getBody().jsonPath().get(),
                "Ответ должен содержать валидный JSON");
        };
    }

    /**
     * Валидатор для ответов с ошибками
     * 
     * Проверяет, что статус код соответствует ожидаемому коду ошибки
     * и тело ответа не пустое (содержит описание ошибки).
     * 
     * @param expectedStatus ожидаемый HTTP статус код ошибки (например, 400, 404, 500)
     * @return Consumer для валидации ответа с ошибкой
     */
    protected Consumer<Response> validateErrorResponse(int expectedStatus) {
        return response -> {
            softAssert.assertEquals(response.getStatusCode(), expectedStatus,
                "Ожидался статус код ошибки " + expectedStatus);
            softAssert.assertNotNull(response.getBody(),
                "Тело ответа с ошибкой не должно быть пустым");
        };
    }

    /**
     * Валидатор для проверки времени ответа
     * 
     * Проверяет, что время ответа не превышает указанное максимальное значение.
     * Используется для проверки производительности API.
     * 
     * @param maxTimeMs максимальное допустимое время ответа в миллисекундах
     * @return Consumer для валидации времени ответа
     */
    protected Consumer<Response> validateResponseTime(long maxTimeMs) {
        return response -> {
            long responseTime = response.getTime();
            softAssert.assertTrue(responseTime <= maxTimeMs,
                "Время ответа " + responseTime + "мс превышает максимальное " + maxTimeMs + "мс");
        };
    }

    /**
     * Вспомогательные методы для создания запросов
     */
    
    /**
     * Добавляет заголовок авторизации Bearer токена к запросу
     * 
     * @param token Bearer токен для авторизации
     * @return функция для добавления заголовка Authorization
     */
    protected Function<RequestSpecification, RequestSpecification> withAuth(String token) {
        return request -> request.header("Authorization", "Bearer " + token);
    }

    /**
     * Добавляет несколько заголовков к запросу
     * 
     * @param headers карта заголовков (ключ - имя заголовка, значение - значение заголовка)
     * @return функция для добавления заголовков
     */
    protected Function<RequestSpecification, RequestSpecification> withHeaders(Map<String, String> headers) {
        return request -> {
            headers.forEach(request::header);
            return request;
        };
    }

    /**
     * Добавляет query параметры к запросу
     * 
     * @param params карта параметров (ключ - имя параметра, значение - значение параметра)
     * @return функция для добавления query параметров
     */
    protected Function<RequestSpecification, RequestSpecification> withQueryParams(Map<String, Object> params) {
        return request -> {
            params.forEach(request::queryParam);
            return request;
        };
    }

    /**
     * Устанавливает базовый URL для запроса
     * 
     * @param url базовый URL API (например, "https://api.example.com")
     * @return функция для установки базового URL
     */
    protected Function<RequestSpecification, RequestSpecification> withBaseUrl(String url) {
        return request -> request.baseUri(url);
    }

    /**
     * Вспомогательные методы для создания действий
     */
    
    /**
     * Создает функцию для выполнения GET запроса
     * 
     * @param endpoint путь к API endpoint (например, "/api/users/123")
     * @return функция для выполнения GET запроса
     */
    protected Function<RequestSpecification, Response> get(String endpoint) {
        return request -> request.when().get(endpoint);
    }

    /**
     * Создает функцию для выполнения POST запроса
     * 
     * @param endpoint путь к API endpoint (например, "/api/users")
     * @param body тело запроса (будет сериализовано в JSON)
     * @return функция для выполнения POST запроса
     */
    protected Function<RequestSpecification, Response> post(String endpoint, Object body) {
        return request -> request.body(body).when().post(endpoint);
    }

    /**
     * Создает функцию для выполнения PUT запроса
     * 
     * @param endpoint путь к API endpoint (например, "/api/users/123")
     * @param body тело запроса (будет сериализовано в JSON)
     * @return функция для выполнения PUT запроса
     */
    protected Function<RequestSpecification, Response> put(String endpoint, Object body) {
        return request -> request.body(body).when().put(endpoint);
    }

    /**
     * Создает функцию для выполнения DELETE запроса
     * 
     * @param endpoint путь к API endpoint (например, "/api/users/123")
     * @return функция для выполнения DELETE запроса
     */
    protected Function<RequestSpecification, Response> delete(String endpoint) {
        return request -> request.when().delete(endpoint);
    }

    /**
     * Логирует детали ответа при ошибке
     * 
     * Выводит в лог информацию об ответе для отладки:
     * - HTTP статус код
     * - Время ответа
     * - Content-Type
     * - Тело ответа (первые 500 символов)
     * 
     * @param response объект Response для логирования
     */
    private void logResponseDetails(Response response) {
        try {
            logger.error("Детали ответа:");
            logger.error("  Статус: {}", response.getStatusCode());
            logger.error("  Время ответа: {} мс", response.getTime());
            logger.error("  Content-Type: {}", response.getContentType());

            if (response.getBody() != null) {
                String body = response.getBody().asString();
                if (body.length() > 500) {
                    body = body.substring(0, 500) + "...";
                }
                logger.error("  Тело ответа: {}", body);
            }
        } catch (Exception e) {
            logger.error("Не удалось залогировать детали ответа: {}", e.getMessage());
        }
    }

    /**
     * Создает тестовые данные для API запросов
     * 
     * Генерирует объекты тестовых данных на основе типа и свойств.
     * Поддерживает предопределенные типы: "user", "product".
     * Для других типов возвращает переданные свойства как есть.
     * 
     * @param type тип тестовых данных ("user", "product" или другой)
     * @param properties карта свойств для создания объекта
     * @return объект тестовых данных (Map или другой объект)
     */
    protected Object createTestData(String type, Map<String, Object> properties) {
        switch (type.toLowerCase()) {
            case "user":
                return Map.of(
                    "name", properties.getOrDefault("name", "Test User"),
                    "email", properties.getOrDefault("email", "test@example.com"),
                    "active", properties.getOrDefault("active", true)
                );
            case "product":
                return Map.of(
                    "name", properties.getOrDefault("name", "Test Product"),
                    "price", properties.getOrDefault("price", 100.0),
                    "category", properties.getOrDefault("category", "test")
                );
            default:
                return properties;
        }
    }

    /**
     * Финализирует все проверки SoftAssert
     * 
     * Вызывает assertAll() для выполнения всех накопленных проверок.
     * Должен вызываться в конце теста для выполнения всех soft assertions.
     */
    protected void finalizeAssertions() {
        softAssert.assertAll();
    }
// }