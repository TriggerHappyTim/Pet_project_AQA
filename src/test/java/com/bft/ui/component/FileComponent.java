package com.bft.ui.component;

import com.bft.ui.core.element.ElementFactory;
import com.bft.pw.By;

import java.io.File;

/**
 * Компонент для работы с загрузкой файлов
 * 
 * Предоставляет унифицированный интерфейс для взаимодействия с file input элементами:
 * - Загрузка файлов по абсолютному пути
 * - Загрузка файлов из classpath
 * - Множественная загрузка файлов
 * - Проверка выбранных файлов
 * - Работа с XML отчетами
 * 
 * <p>Все factory методы статические и возвращают готовый к использованию компонент.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Загрузка файла из classpath
 * FileComponent.createModalFileInput("report").uploadFromClasspath("reports/efs1_sample.xml");
 * 
 * // Загрузка XML отчета по типу
 * FileComponent.createIdFileInput("fileInput", "Отчет").uploadReportXml("EFS-1");
 * 
 * // Загрузка файла по абсолютному пути
 * FileComponent.createNamedFileInput("document", "Документ").uploadFile("/path/to/file.pdf");
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see BaseComponent для базовой функциональности
 * @since 1.0
 */
public class FileComponent extends BaseComponent {

    /**
     * Создает компонент загрузки файла с CSS селектором
     * 
     * @param fileSelector CSS селектор file input элемента
     * @param fileName имя компонента для логирования
     */
    public FileComponent(String fileSelector, String fileName) {
        super(ElementFactory.css(fileSelector).named(fileName).waitVisible().build(), fileName);
    }

    /**
     * Создает компонент загрузки файла с By селектором
     * 
     * Рекомендуемый способ создания компонента, так как By.xpath
     * более универсален для сложных селекторов.
     * 
     * @param fileLocator By селектор file input элемента (обычно XPath)
     * @param fileName имя компонента для логирования
     */
    public FileComponent(By fileLocator, String fileName) {
        super(ElementFactory.by(fileLocator).named(fileName).waitVisible().build(), fileName);
    }

    @Override
    public boolean isValid() {
        return isPresent() && isVisible();
    }

    /**
     * Загружает файл по абсолютному пути
     * 
     * Загружает файл с файловой системы по указанному пути.
     * 
     * @param filePath абсолютный путь к файлу для загрузки
     * @return текущий экземпляр FileComponent для цепочки вызовов
     * @throws IllegalArgumentException если файл не найден по указанному пути
     */
    public FileComponent uploadFile(String filePath) {
        File file = new File(filePath);
        logger.info("Загружаем файл '{}' в компонент: {}", file.getAbsolutePath(), componentName);

        if (!file.exists()) {
            throw new IllegalArgumentException("Файл не найден: " + filePath);
        }

        rootElement.uploadFile(file);
        logger.info("Файл '{}' успешно загружен", file.getName());
        return this;
    }

    /**
     * Загружает файл из classpath
     * 
     * Загружает файл из ресурсов проекта (папка src/test/resources).
     * Путь указывается относительно classpath.
     * 
     * @param fileName имя файла или путь относительно classpath (например, "reports/efs1_sample.xml")
     * @return текущий экземпляр FileComponent для цепочки вызовов
     */
    public FileComponent uploadFromClasspath(String fileName) {
        logger.info("Загружаем файл '{}' из classpath в компонент: {}", fileName, componentName);
        rootElement.uploadFromClasspath(fileName);
        logger.info("Файл '{}' из classpath успешно загружен", fileName);
        return this;
    }

    /**
     * Загружает несколько файлов из classpath
     * 
     * Загружает несколько файлов одновременно (если file input поддерживает multiple).
     * 
     * @param fileNames массив имен файлов или путей относительно classpath
     * @return текущий экземпляр FileComponent для цепочки вызовов
     */
    public FileComponent uploadMultipleFromClasspath(String... fileNames) {
        logger.info("Загружаем {} файлов из classpath в компонент: {}", fileNames.length, componentName);
        rootElement.uploadFromClasspath(fileNames);
        logger.info("Файлы из classpath успешно загружены");
        return this;
    }

    /**
     * Проверяет, что файл выбран
     */
    public boolean hasFileSelected() {
        try {
            String value = rootElement.getAttribute("value");
            boolean hasFile = value != null && !value.trim().isEmpty();
            logger.debug("Проверка выбора файла в компоненте '{}' : {}", componentName, hasFile);
            return hasFile;
        } catch (Exception e) {
            logger.warn("Не удалось проверить выбор файла в компоненте '{}': {}", componentName, e.getMessage());
            return false;
        }
    }

    /**
     * Получает имя выбранного файла
     */
    public String getSelectedFileName() {
        try {
            String value = rootElement.getAttribute("value");
            if (value != null && !value.isEmpty()) {
                // В разных браузерах значение может быть разным
                // Иногда полный путь, иногда только имя файла
                String fileName = value;
                if (value.contains("\\")) {
                    fileName = value.substring(value.lastIndexOf("\\") + 1);
                } else if (value.contains("/")) {
                    fileName = value.substring(value.lastIndexOf("/") + 1);
                }
                logger.debug("Получено имя файла '{}' из компонента: {}", fileName, componentName);
                return fileName;
            }
            return "";
        } catch (Exception e) {
            logger.warn("Не удалось получить имя файла из компонента '{}': {}", componentName, e.getMessage());
            return "";
        }
    }

    /**
     * Очищает выбор файла
     */
    public FileComponent clearSelection() {
        logger.info("Очищаем выбор файла в компоненте: {}", componentName);
        // В HTML5 нет стандартного способа очистить file input
        // Можно использовать JavaScript или создать новый элемент
        try {
            rootElement.executeJavaScript("arguments[0].value = '';", rootElement.getSelenideElement());
            logger.info("Выбор файла очищен");
        } catch (Exception e) {
            logger.warn("Не удалось очистить выбор файла в компоненте '{}': {}", componentName, e.getMessage());
        }
        return this;
    }

    // ===== СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ РАЗЛИЧНЫХ ТИПОВ FILE INPUT =====

    /**
     * Создает компонент для загрузки файла в модальном окне
     */
    public static FileComponent createModalFileInput(String fileName) {
        return new FileComponent(
            By.xpath("//div[@class = 'modal-content']//input[@type = 'file']"),
            "File input в модальном окне '" + fileName + "'"
        );
    }

    /**
     * Создает компонент для загрузки файла по ID
     */
    public static FileComponent createIdFileInput(String inputId, String fileName) {
        return new FileComponent(
            By.xpath("//input[@id='" + inputId + "'][@type='file']"),
            "File input '" + fileName + "'"
        );
    }

    /**
     * Создает компонент для загрузки файла по имени
     */
    public static FileComponent createNamedFileInput(String inputName, String fileName) {
        return new FileComponent(
            By.xpath("//input[@name='" + inputName + "'][@type='file']"),
            "File input '" + fileName + "'"
        );
    }

    /**
     * Создает компонент для загрузки файла по любому XPath
     */
    public static FileComponent createCustomFileInput(String xpath, String fileName) {
        return new FileComponent(By.xpath(xpath), "File input '" + fileName + "'");
    }

    // ===== СПЕЦИАЛИЗИРОВАННЫЕ МЕТОДЫ =====

    /**
     * Загружает XML файл отчета
     */
    public FileComponent uploadReportXml(String reportType) {
        String fileName = getReportFileName(reportType);
        logger.info("Загружаем XML отчет '{}' типа '{}' в компонент: {}", fileName, reportType, componentName);
        return uploadFromClasspath("application/" + reportType.toLowerCase() + "/" + fileName);
    }

    /**
     * Загружает тестовый файл
     */
    public FileComponent uploadTestFile(String fileName) {
        logger.info("Загружаем тестовый файл '{}' в компонент: {}", fileName, componentName);
        return uploadFromClasspath("application/" + fileName);
    }

    /**
     * Проверяет типы разрешенных файлов
     */
    public String getAcceptedTypes() {
        try {
            return rootElement.getAttribute("accept");
        } catch (Exception e) {
            logger.warn("Не удалось получить разрешенные типы файлов для компонента '{}': {}", componentName, e.getMessage());
            return "";
        }
    }

    /**
     * Проверяет, разрешена ли множественная загрузка
     */
    public boolean allowsMultipleFiles() {
        try {
            String multiple = rootElement.getAttribute("multiple");
            return multiple != null;
        } catch (Exception e) {
            logger.warn("Не удалось проверить множественную загрузку для компонента '{}': {}", componentName, e.getMessage());
            return false;
        }
    }

    /**
     * Получает максимальный размер файла (если указан)
     */
    public String getMaxFileSize() {
        // В HTML нет стандартного атрибута для max file size
        // Это обычно проверяется на стороне сервера
        logger.debug("Max file size проверяется на стороне сервера для компонента: {}", componentName);
        return "Проверяется на стороне сервера";
    }

    // ===== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =====

    private String getReportFileName(String reportType) {
        // Логика определения имени файла по типу отчета
        switch (reportType.toUpperCase()) {
            case "SZV-TD":
                return "СЗВ-ТД (2).xml";
            case "SZV-STAJ":
                return "СЗВ-СТАЖ (2).xml";
            case "SZV-M":
                return "szv-m-001.xml";
            case "EFS-1":
                return "EFS-1_ish_DSOL.xml";
            default:
                return reportType.toLowerCase() + ".xml";
        }
    }
}