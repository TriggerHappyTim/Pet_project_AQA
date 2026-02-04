// ============================================================================
// ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется в проекте
// ============================================================================
// package com.bft.integration.config;

import com.bft.security.CredentialManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Конфигурация для интеграции с Jira и Zephyr
 * 
 * <p>Загружает настройки из credentials.properties или переменных окружения.
 * 
 * <p><b>Пример использования:</b>
 * <pre>
 * {@code
 * IntegrationConfig config = IntegrationConfig.fromCredentials();
 * if (config.isZephyrEnabled()) {
 *     ZephyrClient client = ZephyrClientFactory.create(config);
 *     // ... работа с клиентом
 * }
 * }
 * </pre>
 */
public class IntegrationConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(IntegrationConfig.class);
    
    private final boolean zephyrEnabled;
    private final ZephyrType zephyrType;
    private final String jiraUrl;
    private final String jiraUsername;
    private final String jiraApiToken;
    private final String jiraProjectKey;
    private final String zephyrProjectKey;
    private final String zephyrTestCycleKey;
    
    /**
     * Конструктор с параметрами
     */
    public IntegrationConfig(
            boolean zephyrEnabled,
            ZephyrType zephyrType,
            String jiraUrl,
            String jiraUsername,
            String jiraApiToken,
            String jiraProjectKey,
            String zephyrProjectKey,
            String zephyrTestCycleKey) {
        this.zephyrEnabled = zephyrEnabled;
        this.zephyrType = zephyrType;
        this.jiraUrl = jiraUrl;
        this.jiraUsername = jiraUsername;
        this.jiraApiToken = jiraApiToken;
        this.jiraProjectKey = jiraProjectKey;
        this.zephyrProjectKey = zephyrProjectKey;
        this.zephyrTestCycleKey = zephyrTestCycleKey;
    }
    
    /**
     * Создать конфигурацию из credentials.properties или переменных окружения
     * 
     * @return конфигурация интеграции
     */
    public static IntegrationConfig fromCredentials() {
        CredentialManager cm = CredentialManager.getInstance();
        
        boolean enabled = Boolean.parseBoolean(
            getCredentialOrDefault(cm, "zephyr.enabled", "false")
        );
        
        if (!enabled) {
            logger.info("Zephyr integration disabled");
            return createDisabledConfig();
        }
        
        try {
            String zephyrTypeStr = getCredentialOrDefault(cm, "zephyr.type", "scale");
            ZephyrType type = ZephyrType.fromCode(zephyrTypeStr);
            
            String jiraUrl = getRequiredCredential(cm, "jira.url");
            String jiraUsername = getRequiredCredential(cm, "jira.username");
            String jiraApiToken = getRequiredCredential(cm, "jira.api.token");
            String jiraProjectKey = getRequiredCredential(cm, "jira.project.key");
            String zephyrProjectKey = getCredentialOrDefault(cm, "zephyr.project.key", jiraProjectKey);
            String zephyrTestCycleKey = getCredentialOrDefault(cm, "zephyr.test.cycle.key", "AUTO-CYCLE");
            
            logger.info("Zephyr integration enabled: type={}, project={}", type.getDisplayName(), zephyrProjectKey);
            
            return new IntegrationConfig(
                true,
                type,
                jiraUrl,
                jiraUsername,
                jiraApiToken,
                jiraProjectKey,
                zephyrProjectKey,
                zephyrTestCycleKey
            );
            
        } catch (Exception e) {
            logger.error("Failed to load Jira/Zephyr configuration: {}", e.getMessage());
            logger.warn("Zephyr integration will be disabled");
            return createDisabledConfig();
        }
    }
    
    /**
     * Создать конфигурацию с отключенной интеграцией
     */
    private static IntegrationConfig createDisabledConfig() {
        return new IntegrationConfig(
            false,
            ZephyrType.SCALE,
            "",
            "",
            "",
            "",
            "",
            ""
        );
    }
    
    /**
     * Получить credential или значение по умолчанию
     */
    private static String getCredentialOrDefault(CredentialManager cm, String key, String defaultValue) {
        String value = cm.getCredential(key);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }
    
    /**
     * Получить обязательный credential
     * 
     * @throws IllegalStateException если credential не найден
     */
    private static String getRequiredCredential(CredentialManager cm, String key) {
        String value = cm.getCredential(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                String.format("Required credential '%s' not found. " +
                    "Please add it to credentials.properties or set environment variable.", key)
            );
        }
        return value;
    }
    
    // Getters
    
    public boolean isZephyrEnabled() {
        return zephyrEnabled;
    }
    
    public ZephyrType getZephyrType() {
        return zephyrType;
    }
    
    public String getJiraUrl() {
        return jiraUrl;
    }
    
    public String getJiraUsername() {
        return jiraUsername;
    }
    
    public String getJiraApiToken() {
        return jiraApiToken;
    }
    
    public String getJiraProjectKey() {
        return jiraProjectKey;
    }
    
    public String getZephyrProjectKey() {
        return zephyrProjectKey;
    }
    
    public String getZephyrTestCycleKey() {
        return zephyrTestCycleKey;
    }
    
    /**
     * Проверить валидность конфигурации
     * 
     * @return true, если конфигурация валидна
     */
    public boolean isValid() {
        if (!zephyrEnabled) {
            return true; // disabled config is valid
        }
        
        return jiraUrl != null && !jiraUrl.isEmpty()
            && jiraUsername != null && !jiraUsername.isEmpty()
            && jiraApiToken != null && !jiraApiToken.isEmpty()
            && jiraProjectKey != null && !jiraProjectKey.isEmpty();
    }
    
    @Override
    public String toString() {
        return "IntegrationConfig{" +
            "enabled=" + zephyrEnabled +
            ", type=" + zephyrType +
            ", jiraUrl='" + jiraUrl + '\'' +
            ", jiraUsername='" + jiraUsername + '\'' +
            ", projectKey='" + jiraProjectKey + '\'' +
            ", zephyrProjectKey='" + zephyrProjectKey + '\'' +
            ", testCycleKey='" + zephyrTestCycleKey + '\'' +
            '}';
    }
// }
