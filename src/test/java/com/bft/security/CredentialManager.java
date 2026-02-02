package com.bft.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер учетных данных
 * Управляет провайдерами и обеспечивает безопасное получение credentials
 */
public class CredentialManager {

    private static final Logger logger = LoggerFactory.getLogger(CredentialManager.class);

    private static volatile CredentialManager instance;
    private final List<CredentialProvider> providers;

    private CredentialManager() {
        providers = new ArrayList<>();
        initializeProviders();
    }

    /**
     * Получить экземпляр CredentialManager (Singleton)
     */
    public static CredentialManager getInstance() {
        if (instance == null) {
            synchronized (CredentialManager.class) {
                if (instance == null) {
                    instance = new CredentialManager();
                }
            }
        }
        return instance;
    }

    /**
     * Инициализировать провайдеры в правильном порядке приоритета
     */
    private void initializeProviders() {
        // 1. Environment provider (высокий приоритет)
        providers.add(new EnvironmentCredentialProvider());

        // 2. Properties provider (средний приоритет)
        try {
            com.bft.security.providers.PropertiesCredentialProvider propsProvider =
                new com.bft.security.providers.PropertiesCredentialProvider();
            if (propsProvider.isPropertiesLoaded()) {
                providers.add(propsProvider);
                logger.info("Загружено {} credentials из файла свойств", propsProvider.getCredentialsCount());
            }
        } catch (Exception e) {
            logger.warn("Не удалось загрузить провайдер свойств: {}", e.getMessage());
        }

        // 3. Mock provider — fallback для локального запуска и CI, когда нет env/properties
        providers.add(new MockCredentialProvider());

        logger.info("Инициализировано {} провайдеров учетных данных", providers.size());
    }

    /**
     * Получить учетные данные по ключу
     * Проходит по всем провайдерам в порядке приоритета
     */
    public String getCredential(String key) {
        if (key == null || key.trim().isEmpty()) {
            logger.warn("Попытка получить credential с пустым ключом");
            return null;
        }

        for (CredentialProvider provider : providers) {
            try {
                String value = provider.getCredential(key);
                if (value != null) {
                    logger.debug("Найден credential '{}' в провайдере: {}", key, provider.getType());
                    return value;
                }
            } catch (Exception e) {
                logger.warn("Ошибка при получении credential '{}' из провайдера {}: {}",
                           key, provider.getType(), e.getMessage());
            }
        }

        logger.warn("Credential '{}' не найден ни в одном провайдере", key);
        return null;
    }

    /**
     * Получить учетные данные с дефолтным значением
     */
    public String getCredential(String key, String defaultValue) {
        String value = getCredential(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Проверить доступность учетных данных
     */
    public boolean hasCredential(String key) {
        return getCredential(key) != null;
    }

    /**
     * Получить пользовательские credentials
     */
    public EnvironmentCredentialProvider.UserCredentials getUserCredentials(String userPrefix) {
        EnvironmentCredentialProvider.UserCredentials credentials = null;

        // Check all providers for user credentials
        for (CredentialProvider provider : providers) {
            try {
                if (provider instanceof EnvironmentCredentialProvider) {
                    credentials = ((EnvironmentCredentialProvider) provider).getUserCredentials(userPrefix);
                } else if (provider instanceof com.bft.security.providers.PropertiesCredentialProvider) {
                    // For PropertiesCredentialProvider, we need to construct UserCredentials manually
                    com.bft.security.providers.PropertiesCredentialProvider propsProvider =
                        (com.bft.security.providers.PropertiesCredentialProvider) provider;
                    String username = propsProvider.getCredential(userPrefix + ".username");
                    String password = propsProvider.getCredential(userPrefix + ".password");
                    String email = propsProvider.getCredential(userPrefix + ".email");

                    if (username != null || password != null) {
                        credentials = new EnvironmentCredentialProvider.UserCredentials(username, password, email);
                    }
                }

                if (credentials != null && credentials.isValid()) {
                    logger.debug("Найдены пользовательские credentials для '{}' в провайдере {}", userPrefix, provider.getType());
                    break;
                }
            } catch (Exception e) {
                logger.warn("Ошибка при получении credentials из провайдера {}: {}", provider.getType(), e.getMessage());
            }
        }

        if (credentials == null || !credentials.isValid()) {
            logger.warn("Не найдены валидные пользовательские credentials для '{}'", userPrefix);
        }

        return credentials;
    }

    /**
     * Получить API credentials
     */
    public EnvironmentCredentialProvider.ApiCredentials getApiCredentials(String apiPrefix) {
        EnvironmentCredentialProvider.ApiCredentials credentials = null;

        for (CredentialProvider provider : providers) {
            if (provider instanceof EnvironmentCredentialProvider) {
                credentials = ((EnvironmentCredentialProvider) provider).getApiCredentials(apiPrefix);
                if (credentials.hasValidToken() || credentials.hasValidKeySecret()) {
                    logger.debug("Найдены API credentials для '{}'", apiPrefix);
                    break;
                }
            }
        }

        if (credentials == null || (!credentials.hasValidToken() && !credentials.hasValidKeySecret())) {
            logger.warn("Не найдены валидные API credentials для '{}'", apiPrefix);
        }

        return credentials;
    }

    /**
     * Получить database credentials
     */
    public EnvironmentCredentialProvider.DatabaseCredentials getDatabaseCredentials(String dbPrefix) {
        EnvironmentCredentialProvider.DatabaseCredentials credentials = null;

        for (CredentialProvider provider : providers) {
            if (provider instanceof EnvironmentCredentialProvider) {
                credentials = ((EnvironmentCredentialProvider) provider).getDatabaseCredentials(dbPrefix);
                if (credentials.isValid()) {
                    logger.debug("Найдены database credentials для '{}'", dbPrefix);
                    break;
                }
            }
        }

        if (credentials == null || !credentials.isValid()) {
            logger.warn("Не найдены валидные database credentials для '{}'", dbPrefix);
        }

        return credentials;
    }

    /**
     * Добавить провайдер учетных данных
     */
    public void addProvider(CredentialProvider provider) {
        if (provider != null) {
            providers.add(0, provider); // Добавляем в начало для более высокого приоритета
            logger.info("Добавлен провайдер учетных данных: {}", provider.getType());
        }
    }

    /**
     * Удалить провайдер учетных данных
     */
    public void removeProvider(CredentialProvider provider) {
        if (providers.remove(provider)) {
            logger.info("Удален провайдер учетных данных: {}", provider.getType());
        }
    }

    /**
     * Получить список активных провайдеров
     */
    public List<CredentialProvider> getProviders() {
        return new ArrayList<>(providers);
    }

    /**
     * Очистить все провайдеры (для тестирования)
     */
    public void clearProviders() {
        providers.clear();
        logger.info("Очищены все провайдеры учетных данных");
    }

    /**
     * Сбросить экземпляр (для тестирования)
     */
    public static void reset() {
        instance = null;
    }
}