package com.bft.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Централизованное чтение конфигурации из файлов
 * 
 * Предоставляет единый интерфейс для чтения конфигурационных данных из различных источников:
 * - Properties файлы (.properties)
 * - YAML файлы (планируется)
 * - Переменные окружения (через System.getenv)
 * - Системные свойства (через System.getProperty)
 * 
 * <p>Поддерживает профили конфигурации (dev, test, uat, prod) и кеширование
 * загруженных конфигураций для оптимизации производительности.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Чтение из properties файла
 * ConfigReader reader = ConfigReader.fromProperties("config/test.properties");
 * String apiUrl = reader.getProperty("api.base.url");
 * 
 * // Чтение с профилем
 * ConfigReader prodConfig = ConfigReader.fromProperties("config/application.properties", "prod");
 * String dbUrl = prodConfig.getProperty("database.url");
 * 
 * // Чтение с fallback на переменные окружения
 * String browser = reader.getProperty("browser", System.getenv("BROWSER"), "chrome");
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see TestConfig для использования конфигурации в тестах
 * @since 2.0
 */
public class ConfigReader {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigReader.class);
    
    /**
     * Кеш загруженных конфигураций для оптимизации
     */
    private static final java.util.Map<String, Properties> configCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    /**
     * Загруженные свойства
     */
    private final Properties properties;
    
    /**
     * Профиль конфигурации (dev, test, uat, prod)
     */
    private final String profile;
    
    /**
     * Создает ConfigReader из Properties файла
     * 
     * Загружает конфигурацию из указанного файла. Если файл уже был загружен,
     * использует кешированную версию.
     * 
     * @param configPath путь к properties файлу (относительно classpath или абсолютный)
     * @return новый экземпляр ConfigReader с загруженной конфигурацией
     * @throws RuntimeException если файл не найден или не может быть прочитан
     */
    public static ConfigReader fromProperties(String configPath) {
        return fromProperties(configPath, null);
    }
    
    /**
     * Создает ConfigReader из Properties файла с указанным профилем
     * 
     * Загружает конфигурацию из файла с учетом профиля. Если указан профиль,
     * сначала загружается базовый файл, затем профильный (например, application-prod.properties).
     * 
     * @param configPath путь к базовому properties файлу
     * @param profile профиль конфигурации (dev, test, uat, prod) или null для базовой конфигурации
     * @return новый экземпляр ConfigReader с загруженной конфигурацией
     * @throws RuntimeException если файл не найден или не может быть прочитан
     */
    public static ConfigReader fromProperties(String configPath, String profile) {
        String cacheKey = configPath + (profile != null ? ":" + profile : "");
        
        Properties props = configCache.computeIfAbsent(cacheKey, key -> {
            Properties loadedProps = new Properties();
            
            try {
                // Загружаем базовый файл
                loadProperties(configPath, loadedProps);
                
                // Если указан профиль, загружаем профильный файл
                if (profile != null && !profile.isEmpty()) {
                    String profilePath = configPath.replace(".properties", "-" + profile + ".properties");
                    try {
                        loadProperties(profilePath, loadedProps);
                        logger.info("Загружен профиль конфигурации: {}", profile);
                    } catch (Exception e) {
                        logger.warn("Профильный файл {} не найден, используется базовая конфигурация", profilePath);
                    }
                }
                
                logger.info("Конфигурация загружена из: {}", configPath);
                return loadedProps;
                
            } catch (Exception e) {
                logger.error("Ошибка загрузки конфигурации из {}: {}", configPath, e.getMessage());
                throw new RuntimeException("Failed to load configuration from: " + configPath, e);
            }
        });
        
        return new ConfigReader(props, profile);
    }
    
    /**
     * Загружает свойства из файла
     * 
     * Пытается загрузить файл из classpath, затем из файловой системы.
     * 
     * @param configPath путь к файлу
     * @param props объект Properties для заполнения
     * @throws IOException если файл не найден или не может быть прочитан
     */
    private static void loadProperties(String configPath, Properties props) throws IOException {
        // Пытаемся загрузить из classpath
        InputStream classpathStream = ConfigReader.class.getClassLoader().getResourceAsStream(configPath);
        if (classpathStream != null) {
            try (InputStream is = classpathStream) {
                props.load(is);
            }
            return;
        }
        
        // Пытаемся загрузить из файловой системы
        try (FileInputStream fis = new FileInputStream(configPath)) {
            props.load(fis);
        }
    }
    
    /**
     * Создает ConfigReader из существующего объекта Properties
     * 
     * @param properties объект Properties с конфигурацией
     * @return новый экземпляр ConfigReader
     */
    public static ConfigReader fromProperties(Properties properties) {
        return new ConfigReader(new Properties(properties), null);
    }
    
    /**
     * Приватный конструктор
     * 
     * @param properties загруженные свойства
     * @param profile профиль конфигурации
     */
    private ConfigReader(Properties properties, String profile) {
        this.properties = properties;
        this.profile = profile;
    }
    
    /**
     * Получает значение свойства по ключу
     * 
     * @param key ключ свойства
     * @return значение свойства или null если не найдено
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Получает значение свойства по ключу с значением по умолчанию
     * 
     * @param key ключ свойства
     * @param defaultValue значение по умолчанию, если свойство не найдено
     * @return значение свойства или defaultValue если не найдено
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Получает значение свойства с приоритетом источников
     * 
     * Проверяет источники в следующем порядке:
     * 1. Системные свойства (System.getProperty)
     * 2. Переменные окружения (System.getenv)
     * 3. Загруженные свойства из файла
     * 4. Значение по умолчанию
     * 
     * @param key ключ свойства
     * @param envKey ключ переменной окружения (если отличается от key)
     * @param defaultValue значение по умолчанию
     * @return значение свойства из первого найденного источника
     */
    public String getProperty(String key, String envKey, String defaultValue) {
        // Приоритет 1: Системные свойства
        String systemProp = System.getProperty(key);
        if (systemProp != null && !systemProp.isEmpty()) {
            return systemProp;
        }
        
        // Приоритет 2: Переменные окружения
        String envValue = System.getenv(envKey != null ? envKey : key.toUpperCase().replace(".", "_"));
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        
        // Приоритет 3: Загруженные свойства
        String fileValue = properties.getProperty(key);
        if (fileValue != null && !fileValue.isEmpty()) {
            return fileValue;
        }
        
        // Приоритет 4: Значение по умолчанию
        return defaultValue;
    }
    
    /**
     * Получает целочисленное значение свойства
     * 
     * @param key ключ свойства
     * @param defaultValue значение по умолчанию
     * @return целочисленное значение свойства
     * @throws NumberFormatException если значение не может быть преобразовано в int
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Не удалось преобразовать значение '{}' для ключа '{}' в int, используется значение по умолчанию: {}", 
                    value, key, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Получает булево значение свойства
     * 
     * @param key ключ свойства
     * @param defaultValue значение по умолчанию
     * @return булево значение свойства
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }
    
    /**
     * Получает все загруженные свойства
     * 
     * @return копия объекта Properties с загруженными свойствами
     */
    public Properties getAllProperties() {
        return new Properties(properties);
    }
    
    /**
     * Проверяет наличие свойства
     * 
     * @param key ключ свойства
     * @return true если свойство существует, false в противном случае
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * Очищает кеш конфигураций
     * 
     * Полезно для тестирования или при необходимости перезагрузки конфигурации.
     */
    public static void clearCache() {
        configCache.clear();
        logger.info("Кеш конфигураций очищен");
    }
    
    /**
     * Возвращает профиль конфигурации
     * 
     * @return профиль конфигурации или null если не указан
     */
    public String getProfile() {
        return profile;
    }
}
