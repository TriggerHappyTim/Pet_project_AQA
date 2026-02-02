package com.bft.security.providers;

import com.bft.security.CredentialProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Провайдер учетных данных на основе Properties файлов
 * Безопасно получает credentials из файлов конфигурации
 */
public class PropertiesCredentialProvider implements CredentialProvider {

    private final Properties properties;
    private final String propertiesFile;

    public PropertiesCredentialProvider(String propertiesFile) {
        this.propertiesFile = propertiesFile;
        this.properties = loadProperties();
    }

    public PropertiesCredentialProvider() {
        this("credentials.properties");
    }

    @Override
    public String getCredential(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        return properties.getProperty(key);
    }

    @Override
    public boolean hasCredential(String key) {
        return properties.containsKey(key) && getCredential(key) != null;
    }

    @Override
    public ProviderType getType() {
        return ProviderType.PROPERTIES_FILE;
    }

    /**
     * Загружает properties из classpath
     */
    private Properties loadProperties() {
        Properties props = new Properties();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (inputStream != null) {
                props.load(inputStream);
            } else {
                // Если файл не найден, создаем пустые properties
                System.out.println("Properties file '" + propertiesFile + "' not found in classpath. Using empty properties.");
            }
        } catch (IOException e) {
            System.err.println("Error loading properties file '" + propertiesFile + "': " + e.getMessage());
        }

        return props;
    }

    /**
     * Получить все доступные ключи credentials
     */
    public java.util.Set<String> getAvailableKeys() {
        return properties.stringPropertyNames();
    }

    /**
     * Проверить, загружен ли файл свойств
     */
    public boolean isPropertiesLoaded() {
        return !properties.isEmpty();
    }

    /**
     * Получить количество загруженных credentials
     */
    public int getCredentialsCount() {
        return properties.size();
    }
}