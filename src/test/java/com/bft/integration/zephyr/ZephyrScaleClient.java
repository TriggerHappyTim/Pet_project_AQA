// ============================================================================
// ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется в проекте
// ============================================================================
// package com.bft.integration.zephyr;

import com.bft.integration.mapper.TestResult;
import com.bft.integration.mapper.TestStatus;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Клиент для Zephyr Scale (бывший Zephyr for Jira)
 * 
 * <p>Использует Zephyr Scale REST API v2
 * 
 * <p>Документация API: https://support.smartbear.com/zephyr-scale-cloud/api-docs/
 */
// public class ZephyrScaleClient implements ZephyrClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ZephyrScaleClient.class);
    
    private final String baseUrl;
    private final String apiToken;
    private final String projectKey;
    private final CloseableHttpClient httpClient;
    private final Gson gson;
    
    /**
     * Конструктор ZephyrScaleClient
     * 
     * @param jiraUrl URL Jira
     * @param apiToken API token для Zephyr Scale
     * @param projectKey ключ проекта
     */
    public ZephyrScaleClient(String jiraUrl, String apiToken, String projectKey) {
        this.baseUrl = jiraUrl + "/rest/atm/1.0";
        this.apiToken = apiToken;
        this.projectKey = projectKey;
        this.httpClient = HttpClients.createDefault();
        this.gson = new Gson();
        
        logger.info("Zephyr Scale client initialized: project={}", projectKey);
    }
    
    @Override
    public void publishTestResults(List<TestResult> results) {
        logger.info("Publishing {} test results to Zephyr Scale", results.size());
        
        for (TestResult result : results) {
            try {
                updateTestExecution(
                    result.getTestKey(),
                    result.getStatus(),
                    result.getComment()
                );
            } catch (Exception e) {
                logger.error("Failed to publish result for test {}: {}", 
                    result.getTestKey(), e.getMessage());
            }
        }
        
        logger.info("Test results published successfully");
    }
    
    @Override
    public String createTestCycle(String name, String description) {
        try {
            logger.info("Creating test cycle: {}", name);
            
            JsonObject payload = new JsonObject();
            payload.addProperty("name", name);
            payload.addProperty("description", description);
            payload.addProperty("projectKey", projectKey);
            
            String endpoint = baseUrl + "/testrun";
            HttpPost request = createPostRequest(endpoint, payload);
            
            HttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity());
            
            if (response.getStatusLine().getStatusCode() == 201) {
                JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
                String cycleKey = responseJson.get("key").getAsString();
                logger.info("Test cycle created: {}", cycleKey);
                return cycleKey;
            } else {
                logger.error("Failed to create test cycle: {}", responseBody);
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Failed to create test cycle: {}", e.getMessage());
            return null;
        }
    }
    
    @Override
    public void updateTestExecution(String testKey, TestStatus status, String comment) {
        try {
            logger.debug("Updating test execution: {} -> {}", testKey, status);
            
            JsonObject payload = new JsonObject();
            payload.addProperty("testCaseKey", testKey);
            payload.addProperty("projectKey", projectKey);
            payload.addProperty("status", mapStatusToZephyr(status));
            
            if (comment != null && !comment.isEmpty()) {
                payload.addProperty("comment", comment);
            }
            
            String endpoint = baseUrl + "/testresult";
            HttpPost request = createPostRequest(endpoint, payload);
            
            HttpResponse response = httpClient.execute(request);
            
            if (response.getStatusLine().getStatusCode() == 201) {
                logger.info("Test execution updated: {}", testKey);
            } else {
                String responseBody = EntityUtils.toString(response.getEntity());
                logger.error("Failed to update test execution {}: {}", testKey, responseBody);
            }
            
        } catch (Exception e) {
            logger.error("Failed to update test execution {}: {}", testKey, e.getMessage());
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // Проверить доступность API простым запросом
            String endpoint = baseUrl + "/healthcheck";
            HttpPost request = new HttpPost(endpoint);
            request.setHeader("Authorization", "Bearer " + apiToken);
            
            HttpResponse response = httpClient.execute(request);
            return response.getStatusLine().getStatusCode() == 200;
            
        } catch (Exception e) {
            logger.warn("Zephyr Scale API is not available: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Создать POST запрос с авторизацией
     */
    private HttpPost createPostRequest(String endpoint, JsonObject payload) throws IOException {
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Authorization", "Bearer " + apiToken);
        request.setHeader("Content-Type", "application/json");
        
        StringEntity entity = new StringEntity(gson.toJson(payload), "UTF-8");
        request.setEntity(entity);
        
        return request;
    }
    
    /**
     * Маппинг статуса на формат Zephyr Scale
     */
    private String mapStatusToZephyr(TestStatus status) {
        switch (status) {
            case PASS:
                return "Pass";
            case FAIL:
                return "Fail";
            case SKIP:
                return "Not Executed";
            case BLOCKED:
                return "Blocked";
            case IN_PROGRESS:
                return "In Progress";
            default:
                return "Not Executed";
        }
    }
    
    @Override
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
            logger.debug("Zephyr Scale client closed");
        }
    }
// }
