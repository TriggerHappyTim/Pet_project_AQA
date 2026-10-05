package com.bft.security.providers;

import com.bft.security.CredentialProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Провайдер учетных данных на основе Properties файлов
 * Безопасно получает credentials из файлов конфигурации
 */
public class PropertiesCredentialProvider implements CredentialProvider {

    private static final Logger log = LoggerFactory.getLogger(PropertiesCredentialProvider.class);

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
     * Загружает properties из classpath.
     *
     * <p>ВАЖНО: читаем через Reader в UTF-8. Вариант {@code props.load(InputStream)}
     * по спецификации java.util.Properties декодирует файл как ISO-8859-1,
     * из-за чего кириллические значения (например, названия организаций на ЕПГУ)
     * превращаются в нечитаемый мусор и тесты/шаги авторизации падают.
     */
    private Properties loadProperties() {
        Properties props = new Properties();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (inputStream != null) {
                props.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            } else {
                // Если файл не найден, создаем пустые properties
                log.debug("Properties file '{}' not found in classpath. Using empty properties.", propertiesFile);
            }
        } catch (IOException e) {
            log.warn("Error loading properties file '{}': {}", propertiesFile, e.getMessage());
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