// ============================================================================
// ЗАКОММЕНТИРОВАНО: API тесты не используются в проекте
// ============================================================================
// package com.bft.test.negative;

// import com.bft.test.base.ApiTestBase;  // ЗАКОММЕНТИРОВАНО: API тесты не используются
// import com.bft.test.helpers.AssertionHelper;  // ЗАКОММЕНТИРОВАНО: API тесты не используются
// import io.qameta.allure.*;  // ЗАКОММЕНТИРОВАНО: API тесты не используются
// import io.restassured.response.Response;  // ЗАКОММЕНТИРОВАНО: API тесты не используются
// import org.testng.annotations.DataProvider;  // ЗАКОММЕНТИРОВАНО: API тесты не используются
// import org.testng.annotations.Test;  // ЗАКОММЕНТИРОВАНО: API тесты не используются
// 
// import static io.restassured.RestAssured.given;  // ЗАКОММЕНТИРОВАНО: API тесты не используются

// ЗАКОММЕНТИРОВАНО: API тесты не используются
// /**
//  * Негативные тесты для API
//  * 
//  * <p>Этот класс содержит примеры негативных тестов для API:
//  * - Невалидные данные запросов
//  * - Отсутствующие ресурсы (404)
//  * - Неавторизованные запросы (401)
//  * - Ошибки валидации (400)
//  * - Ошибки сервера (500)
//  * 
//  * @author QA Automation Team
//  * @version 1.0
//  * @since 1.0
//  */
// @Epic("Негативные тесты API")
// @Feature("Обработка ошибок API")
// public class ApiNegativeTestCases extends ApiTestBase {

    /**
     * Тест: Запрос несуществующего ресурса (404)
     * 
     * Проверяет, что API корректно возвращает 404 для несуществующего ресурса.
     */
    // ЗАКОММЕНТИРОВАНО: API тесты не используются
    // @Test(groups = {"api", "negative", "404"})
    // @DisplayName("Запрос несуществующего ресурса")
    // @Description("Проверка обработки 404 ошибки при запросе несуществующего ресурса")
    // @Severity(SeverityLevel.NORMAL)
    // @AllureId("API-NEG-001")
    // public void testNonExistentResource() {
    //     givenWhenThen(
    //         // Given
    //         request -> request.baseUri(getBaseUrl()),
    //         // When
    //         request -> request.when().get("/api/users/999999999"),
    //         // Then
    //         response -> {
    //             response.then().statusCode(404);
    //             
    //             // Дополнительная проверка через softAssert
    //             int actualStatus = response.getStatusCode();
    //             if (actualStatus != 404) {
    //                 throw new AssertionError(
    //                     AssertionHelper.formatApiError(
    //                         "/api/users/999999999",
    //                         404,
    //                         actualStatus,
    //                         response.getBody().asString()
    //                     )
    //                 );
    //             }
    //         },
    //         "Non-Existent Resource Test"
    //     );
    // }

    /**
     * Тест: Неавторизованный запрос (401)
     * 
     * Проверяет, что API корректно возвращает 401 для неавторизованных запросов.
     */
    // ЗАКОММЕНТИРОВАНО: API тесты не используются
    // @Test(groups = {"api", "negative", "401"})
    // @DisplayName("Неавторизованный запрос")
    // @Description("Проверка обработки 401 ошибки при запросе без авторизации")
    // @Severity(SeverityLevel.NORMAL)
    // @AllureId("API-NEG-002")
    // public void testUnauthorizedRequest() {
    //     givenWhenThen(
    //         // Given - запрос без токена авторизации
    //         request -> request.baseUri(getBaseUrl()),
    //         // When
    //         request -> request.when().get("/api/protected/users"),
    //         // Then
    //         response -> {
    //             int actualStatus = response.getStatusCode();
    //             if (actualStatus != 401) {
    //                 throw new AssertionError(
    //                     AssertionHelper.formatApiError(
    //                         "/api/protected/users",
    //                         401,
    //                         actualStatus,
    //                         response.getBody().asString()
    //                     )
    //                 );
    //             }
    //         },
    //         "Unauthorized Request Test"
    //     );
    // }

    /**
     * Тест: Невалидные данные в запросе (400)
     * 
     * Проверяет, что API корректно возвращает 400 для невалидных данных.
     */
    // ЗАКОММЕНТИРОВАНО: API тесты не используются
    // @Test(groups = {"api", "negative", "400", "validation"})
    // @DisplayName("Запрос с невалидными данными")
    // @Description("Проверка обработки 400 ошибки при отправке невалидных данных")
    // @Severity(SeverityLevel.NORMAL)
    // @AllureId("API-NEG-003")
    // public void testInvalidRequestData() {
    //     givenWhenThen(
    //         // Given - запрос с невалидными данными
    //         request -> request
    //             .baseUri(getBaseUrl())
    //             .contentType("application/json")
    //             .body("{\"email\": \"invalid-email\", \"age\": -5}"),
    //         // When
    //         request -> request.when().post("/api/users"),
    //         // Then
    //         response -> {
    //             int actualStatus = response.getStatusCode();
    //             if (actualStatus != 400) {
    //                 throw new AssertionError(
    //                     AssertionHelper.formatApiError(
    //                         "/api/users",
    //                         400,
    //                         actualStatus,
    //                         response.getBody().asString()
    //                     )
    //                 );
    //             }
    //             
    //             // Проверяем, что в ответе есть информация об ошибке валидации
    //             String responseBody = response.getBody().asString();
    //             if (!responseBody.contains("error") && 
    //                 !responseBody.contains("validation") &&
    //                 !responseBody.contains("invalid")) {
    //                 throw new AssertionError(
    //                     AssertionHelper.formatApiError(
    //                         "/api/users",
    //                         400,
    //                         actualStatus,
    //                         "Ответ должен содержать информацию об ошибке валидации"
    //                     )
    //                 );
    //             }
    //         },
    //         "Invalid Request Data Test"
    //     );
    // }

    // ЗАКОММЕНТИРОВАНО: API тесты не используются
    // /**
    //  * DataProvider для различных невалидных email форматов
    //  */
    // @DataProvider(name = "invalidEmails")
    // public Object[][] invalidEmailsProvider() {
    //     return new Object[][] {
    //         {"invalid-email", "Email без @"},
    //         {"@domain.com", "Email без имени"},
    //         {"user@", "Email без домена"},
    //         {"user@domain", "Email без TLD"},
    //         {"user..name@domain.com", "Email с двойными точками"},
    //         {"user@domain..com", "Email с двойными точками в домене"},
    //         {"user name@domain.com", "Email с пробелом"},
    //         {"", "Пустой email"},
    //         {"user@domain@com", "Email с двумя @"},
    //         {"<script>alert('xss')</script>@domain.com", "XSS попытка"},
    //     };
    // }

    /**
     * Параметризованный тест: Валидация email в API
     * 
     * Проверяет, что API корректно отклоняет невалидные email форматы.
     */
    // ЗАКОММЕНТИРОВАНО: API тесты не используются
    // @Test(dataProvider = "invalidEmails", groups = {"api", "negative", "validation", "email", "data-driven"})
    // @DisplayName("Валидация email в API: {1}")
    // @Description("Параметризованный тест для проверки валидации различных невалидных email форматов")
    // @Severity(SeverityLevel.NORMAL)
    // @AllureId("API-NEG-004")
    // public void testEmailValidation(String invalidEmail, String description) {
    //     givenWhenThen(
    //         // Given
    //         request -> request
    //             .baseUri(getBaseUrl())
    //             .contentType("application/json")
    //             .body(String.format("{\"email\": \"%s\", \"name\": \"Test User\"}", invalidEmail)),
    //         // When
    //         request -> request.when().post("/api/users"),
    //         // Then
    //         response -> {
    //             int actualStatus = response.getStatusCode();
    //             if (actualStatus != 400 && actualStatus != 422) {
    //                 throw new AssertionError(
    //                     AssertionHelper.formatApiError(
    //                         "/api/users",
    //                         400,
    //                         actualStatus,
    //                         String.format("Email '%s' (%s) должен быть отклонен", invalidEmail, description)
    //                     )
    //                 );
    //             }
    //         },
    //         String.format("Email Validation Test: %s", description)
    //     );
    // }

    /**
     * Тест: Ошибка сервера (500)
     * 
     * Проверяет обработку ошибок сервера (если есть endpoint, который может вернуть 500).
     */
    // ЗАКОММЕНТИРОВАНО: API тесты не используются
    // @Test(groups = {"api", "negative", "500"})
    // @DisplayName("Ошибка сервера")
    // @Description("Проверка обработки 500 ошибки сервера")
    // @Severity(SeverityLevel.NORMAL)
    // @AllureId("API-NEG-005")
    // public void testServerError() {
    //     // Этот тест может быть специфичным для конкретного API
    //     // Пример: запрос, который вызывает ошибку сервера
    //     givenWhenThen(
    //         // Given
    //         request -> request.baseUri(getBaseUrl()),
    //         // When - запрос, который может вызвать ошибку сервера
    //         // Например, запрос с данными, которые вызывают исключение на сервере
    //         request -> request
    //             .contentType("application/json")
    //             .body("{\"action\": \"cause_server_error\"}")
    //             .when()
    //             .post("/api/test/error"),
    //         // Then
    //         response -> {
    //             // Проверяем, что сервер вернул ошибку (500 или 503)
    //             int statusCode = response.getStatusCode();
    //             if (statusCode < 500 || statusCode >= 600) {
    //                 throw new AssertionError(
    //                     AssertionHelper.formatApiError(
    //                         "/api/test/error",
    //                         500,
    //                         statusCode,
    //                         "Сервер должен вернуть ошибку 5xx"
    //                     )
    //                 );
    //             }
    //         },
    //         "Server Error Test"
    //     );
    // }

    /**
     * Тест: Превышение таймаута запроса
     * 
     * Проверяет обработку ситуации, когда запрос превышает таймаут.
     */
    // ЗАКОММЕНТИРОВАНО: API тесты не используются
    // @Test(groups = {"api", "negative", "timeout"})
    // @DisplayName("Превышение таймаута запроса")
    // @Description("Проверка обработки таймаута при долгом выполнении запроса")
    // @Severity(SeverityLevel.NORMAL)
    // @AllureId("API-NEG-006")
    // public void testRequestTimeout() {
    //     try {
    //         Response response = given()
    //             .baseUri(getBaseUrl())
    //             .timeout(1000)  // Очень короткий таймаут (1 секунда)
    //             .when()
    //             .get("/api/slow-endpoint");  // Предполагается медленный endpoint
    //         
    //         // Если запрос завершился успешно, это может быть проблемой
    //         throw new AssertionError("Запрос не должен завершиться успешно с таким коротким таймаутом");
    //     } catch (Exception e) {
    //         // Ожидаемое поведение - таймаут или ошибка соединения
    //         logger.info("Таймаут обработан корректно: {}", e.getMessage());
    //         if (!e.getMessage().contains("timeout") &&
    //             !e.getMessage().contains("Read timed out") &&
    //             !e.getMessage().contains("Connection")) {
    //             throw new AssertionError(
    //                 AssertionHelper.formatTestError(
    //                     "testRequestTimeout",
    //                     "Исключение должно быть связано с таймаутом",
    //                     e.getMessage()
    //                 )
    //             );
    //         }
    //     }
    // }

    // ЗАКОММЕНТИРОВАНО: API тесты не используются
    // /**
    //  * Вспомогательный метод для получения базового URL API
    //  * 
    //  * @return базовый URL API из конфигурации
    //  */
    // private String getBaseUrl() {
    //     // Получаем URL из конфигурации или системных свойств
    //     String baseUrl = System.getProperty("api.base.url");
    //     if (baseUrl == null || baseUrl.isEmpty()) {
    //         baseUrl = "https://api.example.com";  // Заглушка для примера
    //     }
    //     return baseUrl;
    // }
// }
