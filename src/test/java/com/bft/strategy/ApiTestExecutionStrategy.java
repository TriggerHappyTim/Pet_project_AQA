package com.bft.strategy;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.asserts.SoftAssert;

import java.util.Map;

/**
 * Стратегия выполнения API тестов
 */
public class ApiTestExecutionStrategy extends BaseTestExecutionStrategy<Response> {

    private final String baseUrl;
    private final String endpoint;
    private final String method;
    private final Map<String, Object> headers;
    private final Map<String, Object> queryParams;
    private final Object requestBody;
    private final int expectedStatusCode;

    public ApiTestExecutionStrategy(String baseUrl, String endpoint, String method,
                                  Map<String, Object> headers, Map<String, Object> queryParams,
                                  Object requestBody, int expectedStatusCode) {
        super("API Test Execution", 5);
        this.baseUrl = baseUrl;
        this.endpoint = endpoint;
        this.method = method != null ? method.toUpperCase() : "GET";
        this.headers = headers;
        this.queryParams = queryParams;
        this.requestBody = requestBody;
        this.expectedStatusCode = expectedStatusCode;
    }

    public ApiTestExecutionStrategy(String endpoint, String method, int expectedStatusCode) {
        this("http://localhost:8080", endpoint, method, null, null, null, expectedStatusCode);
    }

    @Override
    public ExecutionStrategyType getType() {
        return ExecutionStrategyType.API_REST_CALL;
    }

    @Override
    public boolean isApplicable(TestContext context) {
        return context.getTestName().toLowerCase().contains("api") ||
               context.getTestName().toLowerCase().contains("rest");
    }

    @Override
    protected void performPreparation(TestContext context) {
        // Настройка RestAssured
        RestAssured.baseURI = baseUrl;

        // Устанавливаем ожидаемый результат
        context.setExpectedResult(expectedStatusCode);

        System.out.println("Подготовка API запроса: " + method + " " + endpoint);
    }

    @Override
    protected void performExecution(TestContext context) {
        RequestSpecification request = RestAssured.given();

        // Добавляем headers
        if (headers != null) {
            request.headers(headers);
        }

        // Добавляем query параметры
        if (queryParams != null) {
            request.queryParams(queryParams);
        }

        // Добавляем тело запроса
        if (requestBody != null) {
            if (requestBody instanceof String) {
                request.body((String) requestBody);
            } else {
                request.body(requestBody);
            }
        }

        // Выполняем запрос
        Response response = null;
        switch (method) {
            case "GET":
                response = request.get(endpoint);
                break;
            case "POST":
                response = request.post(endpoint);
                break;
            case "PUT":
                response = request.put(endpoint);
                break;
            case "DELETE":
                response = request.delete(endpoint);
                break;
            case "PATCH":
                response = request.patch(endpoint);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        context.setActualResult(response);
        System.out.println("API запрос выполнен. Статус: " + response.getStatusCode());
    }

    @Override
    protected void performValidation(TestContext context, SoftAssert softAssert) {
        Response response = (Response) context.getActualResult();
        Integer expectedStatus = (Integer) context.getExpectedResult();

        // Проверяем статус код
        softAssert.assertNotNull(response, "Ответ не должен быть null");
        softAssert.assertEquals(response.getStatusCode(), expectedStatus.intValue(),
            "Статус код должен быть " + expectedStatus);

        // Проверяем время ответа (не более 5 секунд)
        long responseTime = response.getTime();
        softAssert.assertTrue(responseTime < 5000,
            "Время ответа должно быть менее 5 секунд, фактически: " + responseTime + "мс");

        // Проверяем наличие тела ответа
        if (expectedStatus >= 200 && expectedStatus < 300) {
            softAssert.assertNotNull(response.getBody(),
                "Тело ответа не должно быть пустым для успешного запроса");
        }

        // Проверяем корректность JSON, если это JSON ответ
        String contentType = response.getContentType();
        if (contentType != null && contentType.contains("json")) {
            softAssert.assertTrue(isValidJson(response.getBody().asString()),
                "Ответ должен содержать валидный JSON");
        }
    }

    @Override
    protected void performCleanup(TestContext context) {
        // Очистка ресурсов, если необходимо
        Response response = (Response) context.getActualResult();
        if (response != null) {
            // Логируем результат для отладки
            System.out.println("API тест завершен. Статус: " + response.getStatusCode());
        }
    }

    private boolean isValidJson(String json) {
        try {
            RestAssured.given().body(json).when().get("data:text/plain,").then();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}