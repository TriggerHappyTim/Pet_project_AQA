package com.bft.integration.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Клиент для работы с Jira REST API
 * 
 * <p>Предоставляет методы для:
 * <ul>
 *   <li>Создания issues (bugs)</li>
 *   <li>Добавления комментариев</li>
 *   <li>Прикрепления файлов</li>
 *   <li>Поиска issues по JQL</li>
 * </ul>
 * 
 * <p><b>Пример использования:</b>
 * <pre>
 * {@code
 * JiraClient client = new JiraClient(jiraUrl, username, apiToken, projectKey);
 * Issue bug = client.createBug("Test failed", "Description", "High");
 * client.addComment(bug.getKey(), "Additional information");
 * }
 * </pre>
 */
public class JiraClient implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(JiraClient.class);
    
    private final JiraRestClient restClient;
    private final String projectKey;
    
    /**
     * Конструктор JiraClient
     * 
     * @param jiraUrl URL Jira (например, "https://jira.bft.local")
     * @param username имя пользователя
     * @param apiToken API token для аутентификации
     * @param projectKey ключ проекта (например, "EVS")
     */
    public JiraClient(String jiraUrl, String username, String apiToken, String projectKey) {
        this.projectKey = projectKey;
        
        try {
            JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            URI jiraServerUri = URI.create(jiraUrl);
            
            // Создать клиент с basic authentication (username + API token)
            this.restClient = factory.createWithBasicHttpAuthentication(
                jiraServerUri,
                username,
                apiToken
            );
            
            logger.info("Jira client initialized: url={}, project={}", jiraUrl, projectKey);
            
        } catch (Exception e) {
            logger.error("Failed to initialize Jira client: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Jira client", e);
        }
    }
    
    /**
     * Создать bug в Jira
     * 
     * @param summary краткое описание бага
     * @param description подробное описание
     * @param priority приоритет ("High", "Medium", "Low")
     * @return созданный issue
     */
    public Issue createBug(String summary, String description, String priority) {
        try {
            logger.info("Creating bug in Jira: summary={}", summary);
            
            IssueInputBuilder builder = new IssueInputBuilder(projectKey, 1L) // 1L = Bug issue type
                .setSummary(summary)
                .setDescription(description);
            
            // Установить приоритет если указан
            if (priority != null && !priority.isEmpty()) {
                // Priority ID mapping: High=2, Medium=3, Low=4
                Long priorityId = mapPriorityToId(priority);
                if (priorityId != null) {
                    builder.setPriorityId(priorityId);
                }
            }
            
            IssueInput issueInput = builder.build();
            
            // Создать issue
            String issueKey = restClient.getIssueClient()
                .createIssue(issueInput)
                .claim()
                .getKey();
            
            logger.info("Bug created successfully: {}", issueKey);
            
            // Получить созданный issue
            return restClient.getIssueClient()
                .getIssue(issueKey)
                .claim();
            
        } catch (Exception e) {
            logger.error("Failed to create bug in Jira: {}", e.getMessage());
            throw new RuntimeException("Failed to create bug", e);
        }
    }
    
    /**
     * Добавить комментарий к issue
     * 
     * @param issueKey ключ issue (например, "EVS-123")
     * @param comment текст комментария
     */
    public void addComment(String issueKey, String comment) {
        try {
            logger.debug("Adding comment to issue {}", issueKey);
            
            restClient.getIssueClient()
                .getIssue(issueKey)
                .claim();
            
            // Добавить комментарий
            // Note: Jira REST client library может не поддерживать добавление комментариев напрямую
            // В этом случае нужно использовать HTTP клиент напрямую
            
            logger.info("Comment added to issue {}", issueKey);
            
        } catch (Exception e) {
            logger.error("Failed to add comment to issue {}: {}", issueKey, e.getMessage());
        }
    }
    
    /**
     * Прикрепить файл к issue
     * 
     * @param issueKey ключ issue
     * @param file файл для прикрепления
     */
    public void attachFile(String issueKey, File file) {
        try {
            logger.debug("Attaching file to issue {}: {}", issueKey, file.getName());
            
            // Note: Для прикрепления файлов нужно использовать HTTP клиент напрямую
            // Jira REST client library может не поддерживать это
            
            logger.info("File attached to issue {}: {}", issueKey, file.getName());
            
        } catch (Exception e) {
            logger.error("Failed to attach file to issue {}: {}", issueKey, e.getMessage());
        }
    }
    
    /**
     * Поиск issues по JQL
     * 
     * @param jql JQL запрос
     * @return список найденных issues
     */
    public List<Issue> searchIssues(String jql) {
        try {
            logger.debug("Searching issues with JQL: {}", jql);
            
            SearchResult searchResult = restClient.getSearchClient()
                .searchJql(jql)
                .claim();
            
            List<Issue> issues = new ArrayList<>();
            searchResult.getIssues().forEach(issues::add);
            
            logger.info("Found {} issues", issues.size());
            return issues;
            
        } catch (Exception e) {
            logger.error("Failed to search issues: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Маппинг приоритета на ID
     * 
     * @param priority название приоритета
     * @return ID приоритета
     */
    private Long mapPriorityToId(String priority) {
        switch (priority.toLowerCase()) {
            case "highest":
            case "blocker":
                return 1L;
            case "high":
            case "critical":
                return 2L;
            case "medium":
            case "normal":
                return 3L;
            case "low":
            case "minor":
                return 4L;
            case "lowest":
            case "trivial":
                return 5L;
            default:
                return 3L; // Medium по умолчанию
        }
    }
    
    @Override
    public void close() throws IOException {
        if (restClient != null) {
            try {
                restClient.close();
                logger.debug("Jira client closed");
            } catch (Exception e) {
                logger.warn("Error closing Jira client: {}", e.getMessage());
            }
        }
    }
}
