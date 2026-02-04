// ============================================================================
// ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется в проекте
// ============================================================================
// package com.bft.integration.zephyr;

import com.bft.integration.config.IntegrationConfig;
import com.bft.integration.config.ZephyrType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Фабрика для создания ZephyrClient в зависимости от типа Zephyr
 */
// public class ZephyrClientFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(ZephyrClientFactory.class);
    
    /**
     * Создать ZephyrClient на основе конфигурации
     * 
     * @param config конфигурация интеграции
     * @return экземпляр ZephyrClient
     * @throws IllegalStateException если интеграция отключена или конфигурация невалидна
     */
    public static ZephyrClient create(IntegrationConfig config) {
        if (!config.isZephyrEnabled()) {
            throw new IllegalStateException("Zephyr integration is disabled");
        }
        
        if (!config.isValid()) {
            throw new IllegalStateException("Zephyr configuration is invalid");
        }
        
        ZephyrType type = config.getZephyrType();
        logger.info("Creating Zephyr client: type={}", type.getDisplayName());
        
        switch (type) {
            case SCALE:
                return new ZephyrScaleClient(
                    config.getJiraUrl(),
                    config.getJiraApiToken(),
                    config.getZephyrProjectKey()
                );
                
            case SQUAD:
                return new ZephyrSquadClient(
                    config.getJiraUrl(),
                    config.getJiraApiToken(),
                    config.getZephyrProjectKey()
                );
                
            case STANDALONE:
                throw new UnsupportedOperationException(
                    "Zephyr Standalone is not yet supported"
                );
                
            default:
                throw new IllegalArgumentException(
                    "Unknown Zephyr type: " + type
                );
        }
    }
// }
