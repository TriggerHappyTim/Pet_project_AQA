package com.bft.utils;

import com.bft.pw.SelenideElement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.bft.pw.PwDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bft.pw.WebDriverRunner.getWebDriver;

/**
 * Утилита для парсинга структуры формы и сохранения метаданных полей
 * 
 * Позволяет извлекать информацию о полях формы, сохранять её в JSON файл
 * для дальнейшего использования в отладке и создании тестов.
 * 
 * @author QA Automation Team
 * @version 1.0
 */
public class FormStructureParser {

    private static final Logger log = LoggerFactory.getLogger(FormStructureParser.class);

    private static final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    
    /**
     * Парсит структуру формы из переданного SelenideElement
     * 
     * Извлекает все поля ввода, селекты, кнопки и другие интерактивные элементы,
     * собирая метаданные для каждого поля.
     * 
     * @param formElement элемент формы или диалога для парсинга
     * @return список метаданных полей формы
     */
    public static List<FormFieldMetadata> parseFormStructure(SelenideElement formElement) {
        List<FormFieldMetadata> fields = new ArrayList<>();
        
        try {
            // Получаем WebDriver для выполнения JavaScript
            PwDriver driver = getWebDriver();
                        
            // Извлекаем все интерактивные элементы
            var inputs = formElement.$$x(".//input[not(@type='hidden')]");
            var selects = formElement.$$x(".//select");
            var buttons = formElement.$$x(".//button");
            var textareas = formElement.$$x(".//textarea");
            var comboboxes = formElement.$$x(".//*[@role='combobox']");
            
            // Парсим inputs
            for (var input : inputs) {
                if (input.exists() && input.isDisplayed()) {
                    FormFieldMetadata metadata = parseInputElement(input, formElement, driver);
                    if (metadata != null) {
                        fields.add(metadata);
                    }
                }
            }
            
            // Парсим selects
            for (var select : selects) {
                if (select.exists() && select.isDisplayed()) {
                    FormFieldMetadata metadata = parseSelectElement(select, formElement, driver);
                    if (metadata != null) {
                        fields.add(metadata);
                    }
                }
            }
            
            // Парсим buttons (кнопки в формах)
            for (var button : buttons) {
                if (button.exists() && button.isDisplayed()) {
                    FormFieldMetadata metadata = parseButtonElement(button, formElement, driver);
                    if (metadata != null) {
                        fields.add(metadata);
                    }
                }
            }
            
            // Парсим textareas
            for (var textarea : textareas) {
                if (textarea.exists() && textarea.isDisplayed()) {
                    FormFieldMetadata metadata = parseTextareaElement(textarea, formElement, driver);
                    if (metadata != null) {
                        fields.add(metadata);
                    }
                }
            }
            
            // Парсим comboboxes (MUI Autocomplete)
            for (var combobox : comboboxes) {
                if (combobox.exists() && combobox.isDisplayed()) {
                    FormFieldMetadata metadata = parseComboboxElement(combobox, formElement, driver);
                    if (metadata != null) {
                        fields.add(metadata);
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("Ошибка при парсинге структуры формы: " + e.getMessage());
            e.printStackTrace();
        }
        
        return fields;
    }
    
    /**
     * Парсит input элемент
     * 
     * @param input элемент input
     * @param formElement родительский элемент формы
     * @param js JavaScript executor для получения XPath
     * @return метаданные поля или null при ошибке
     */
    private static FormFieldMetadata parseInputElement(SelenideElement input, SelenideElement formElement, PwDriver driver) {
        FormFieldMetadata metadata = new FormFieldMetadata();
        
        try {
            metadata.setFieldType("input");
            metadata.setInputName(input.getAttribute("name"));
            metadata.setInputId(input.getAttribute("id"));
            metadata.setRole(input.getAttribute("role"));
            metadata.setPlaceholder(input.getAttribute("placeholder"));
            metadata.setCurrentValue(input.getValue());
            metadata.setCssClasses(input.getAttribute("class"));
            metadata.setVisible(input.isDisplayed());
            metadata.setEnabled(input.isEnabled());
            
            // Получаем XPath
            String xpath = getElementXPath(input, driver);
            metadata.setXpath(xpath);
            
            // Ищем метку
            String label = findLabelForInput(input, formElement);
            if (label != null && !label.isEmpty()) {
                metadata.setLabelText(label);
                metadata.addLabelVariant(label);
            }
            
            // Собираем все атрибуты
            Map<String, String> attrs = getAllAttributes(input, driver);
            metadata.setAttributes(attrs != null ? attrs : new HashMap<>());
            
        } catch (Exception e) {
            log.warn("Ошибка при парсинге input: " + e.getMessage());
            return null;
        }
        
        return metadata;
    }
    
    /**
     * Парсит select элемент
     * 
     * @param select элемент select
     * @param formElement родительский элемент формы
     * @param js JavaScript executor для получения XPath
     * @return метаданные поля или null при ошибке
     */
    private static FormFieldMetadata parseSelectElement(SelenideElement select, SelenideElement formElement, PwDriver driver) {
        FormFieldMetadata metadata = new FormFieldMetadata();
        
        try {
            metadata.setFieldType("select");
            metadata.setInputName(select.getAttribute("name"));
            metadata.setInputId(select.getAttribute("id"));
            metadata.setCssClasses(select.getAttribute("class"));
            metadata.setVisible(select.isDisplayed());
            metadata.setEnabled(select.isEnabled());
            
            // Получаем XPath
            String xpath = getElementXPath(select, driver);
            metadata.setXpath(xpath);
            
            // Ищем метку
            String label = findLabelForInput(select, formElement);
            if (label != null && !label.isEmpty()) {
                metadata.setLabelText(label);
                metadata.addLabelVariant(label);
            }
            
        } catch (Exception e) {
            log.warn("Ошибка при парсинге select: " + e.getMessage());
            return null;
        }
        
        return metadata;
    }
    
    /**
     * Парсит button элемент
     * 
     * @param button элемент button
     * @param formElement родительский элемент формы
     * @param js JavaScript executor для получения XPath
     * @return метаданные поля или null при ошибке
     */
    private static FormFieldMetadata parseButtonElement(SelenideElement button, SelenideElement formElement, PwDriver driver) {
        FormFieldMetadata metadata = new FormFieldMetadata();
        
        try {
            metadata.setFieldType("button");
            String buttonText = button.getText();
            if (buttonText != null && !buttonText.isEmpty()) {
                metadata.setLabelText(buttonText);
                metadata.addLabelVariant(buttonText);
            }
            metadata.setInputId(button.getAttribute("id"));
            metadata.setCssClasses(button.getAttribute("class"));
            metadata.setVisible(button.isDisplayed());
            metadata.setEnabled(button.isEnabled());
            
            String xpath = getElementXPath(button, driver);
            metadata.setXpath(xpath);
            
        } catch (Exception e) {
            log.warn("Ошибка при парсинге button: " + e.getMessage());
            return null;
        }
        
        return metadata;
    }
    
    /**
     * Парсит textarea элемент
     * 
     * @param textarea элемент textarea
     * @param formElement родительский элемент формы
     * @param js JavaScript executor для получения XPath
     * @return метаданные поля или null при ошибке
     */
    private static FormFieldMetadata parseTextareaElement(SelenideElement textarea, SelenideElement formElement, PwDriver driver) {
        FormFieldMetadata metadata = new FormFieldMetadata();
        
        try {
            metadata.setFieldType("textarea");
            metadata.setInputName(textarea.getAttribute("name"));
            metadata.setInputId(textarea.getAttribute("id"));
            metadata.setPlaceholder(textarea.getAttribute("placeholder"));
            metadata.setCurrentValue(textarea.getValue());
            metadata.setCssClasses(textarea.getAttribute("class"));
            metadata.setVisible(textarea.isDisplayed());
            metadata.setEnabled(textarea.isEnabled());
            
            String xpath = getElementXPath(textarea, driver);
            metadata.setXpath(xpath);
            
            String label = findLabelForInput(textarea, formElement);
            if (label != null && !label.isEmpty()) {
                metadata.setLabelText(label);
                metadata.addLabelVariant(label);
            }
            
        } catch (Exception e) {
            log.warn("Ошибка при парсинге textarea: " + e.getMessage());
            return null;
        }
        
        return metadata;
    }
    
    /**
     * Парсит combobox элемент (MUI Autocomplete)
     * 
     * @param combobox элемент с role="combobox"
     * @param formElement родительский элемент формы
     * @param js JavaScript executor для получения XPath
     * @return метаданные поля или null при ошибке
     */
    private static FormFieldMetadata parseComboboxElement(SelenideElement combobox, SelenideElement formElement, PwDriver driver) {
        FormFieldMetadata metadata = new FormFieldMetadata();
        
        try {
            metadata.setFieldType("combobox");
            metadata.setRole("combobox");
            
            // Для combobox ищем ближайший input
            var input = combobox.$x(".//input[not(@type='hidden')]");
            if (input.exists()) {
                metadata.setInputName(input.getAttribute("name"));
                metadata.setInputId(input.getAttribute("id"));
                metadata.setPlaceholder(input.getAttribute("placeholder"));
                metadata.setCurrentValue(input.getValue());
            } else {
                // Если нет input, используем данные самого элемента
                metadata.setInputId(combobox.getAttribute("id"));
            }
            
            metadata.setCssClasses(combobox.getAttribute("class"));
            metadata.setVisible(combobox.isDisplayed());
            metadata.setEnabled(combobox.isEnabled());
            
            String xpath = getElementXPath(combobox, driver);
            metadata.setXpath(xpath);
            
            // Ищем метку для combobox (обычно находится рядом или в родительском контейнере)
            String label = findLabelForCombobox(combobox, formElement);
            if (label != null && !label.isEmpty()) {
                metadata.setLabelText(label);
                metadata.addLabelVariant(label);
            }
            
        } catch (Exception e) {
            log.warn("Ошибка при парсинге combobox: " + e.getMessage());
            return null;
        }
        
        return metadata;
    }
    
    /**
     * Ищет метку для input/select/textarea
     * 
     * Использует несколько стратегий поиска метки:
     * 1. Label с атрибутом for (связь по id)
     * 2. Label как родитель элемента
     * 3. Label как предыдущий sibling
     * 4. Label в родительском контейнере MuiFormControl
     * 5. Текст перед элементом в родителе
     * 
     * @param input элемент input/select/textarea
     * @param formElement родительский элемент формы
     * @return текст метки или null
     */
    private static String findLabelForInput(SelenideElement input, SelenideElement formElement) {
        try {
            // Стратегия 1: label с for атрибутом
            String id = input.getAttribute("id");
            if (id != null && !id.isEmpty()) {
                var label = formElement.$x(".//label[@for='" + id + "']");
                if (label.exists()) {
                    String labelText = label.getText().trim();
                    if (!labelText.isEmpty()) {
                        return labelText;
                    }
                }
            }
            
            // Стратегия 2: label как родитель
            var parentLabel = input.$x("./ancestor::label[1]");
            if (parentLabel.exists()) {
                String labelText = parentLabel.getText().trim();
                if (!labelText.isEmpty()) {
                    return labelText;
                }
            }
            
            // Стратегия 3: Ищем label рядом (предыдущий или следующий sibling)
            var prevLabel = input.$x("./preceding-sibling::label[1]");
            if (prevLabel.exists()) {
                String labelText = prevLabel.getText().trim();
                if (!labelText.isEmpty()) {
                    return labelText;
                }
            }
            
            // Стратегия 4: Ищем в родительском контейнере MuiFormControl
            var formControl = input.$x("./ancestor::*[contains(@class,'MuiFormControl')][1]");
            if (formControl.exists()) {
                var labelInFormControl = formControl.$x(".//label | .//*[contains(@class,'MuiInputLabel')] | .//span[contains(@class,'MuiFormLabel')]");
                if (labelInFormControl.exists()) {
                    String labelText = labelInFormControl.getText().trim();
                    if (!labelText.isEmpty()) {
                        return labelText;
                    }
                }
            }
            
            // Стратегия 5: Ищем текст перед элементом в том же родителе
            var parent = input.parent();
            if (parent.exists()) {
                String textBefore = parent.getText();
                if (textBefore != null && !textBefore.isEmpty()) {
                    // Берем первую строку текста как возможную метку
                    String[] lines = textBefore.split("\n");
                    if (lines.length > 0 && !lines[0].trim().isEmpty()) {
                        return lines[0].trim();
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("Ошибка при поиске метки: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Ищет метку для combobox (MUI Autocomplete)
     * 
     * Использует специальные стратегии для поиска метки в структуре MUI Autocomplete.
     * 
     * @param combobox элемент с role="combobox"
     * @param formElement родительский элемент формы
     * @return текст метки или null
     */
    private static String findLabelForCombobox(SelenideElement combobox, SelenideElement formElement) {
        try {
            // Стратегия 1: Ищем в родительском контейнере MuiFormControl
            var formControl = combobox.$x("./ancestor::*[contains(@class,'MuiFormControl')][1]");
            if (formControl.exists()) {
                var labelInFormControl = formControl.$x(".//label | .//*[contains(@class,'MuiInputLabel')] | .//span[contains(@class,'MuiFormLabel')] | .//*[contains(@class,'label')]");
                if (labelInFormControl.exists()) {
                    String labelText = labelInFormControl.getText().trim();
                    if (!labelText.isEmpty()) {
                        return labelText;
                    }
                }
            }
            
            // Стратегия 2: Ищем текст перед combobox в родительском контейнере
            var parent = combobox.$x("./ancestor::div[contains(@class,'MuiFormControl') or contains(@class,'form') or contains(@class,'field')][1]");
            if (parent.exists()) {
                // Ищем все текстовые элементы перед input внутри combobox
                var labels = parent.$$x(".//*[contains(., '')]");
                for (var label : labels) {
                    try {
                        String text = label.getText().trim();
                        if (!text.isEmpty() && text.length() < 50 && !text.contains("input")) {
                            // Проверяем, что это не сам input
                            var isInput = label.$x(".//input");
                            if (!isInput.exists()) {
                                return text;
                            }
                        }
                    } catch (Exception ignored) {
                        // Продолжаем поиск
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("Ошибка при поиске метки для combobox: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Получает XPath элемента через JavaScript
     * 
     * @param element элемент для получения XPath
     * @param js JavaScript executor
     * @return XPath строка
     */
    private static String getElementXPath(SelenideElement element, PwDriver driver) {
        try {
            return (String) driver.executeScript(
                "function getElementXPath(element) { " +
                "  if (element.id !== '') return '//*[@id=\"' + element.id + '\"]'; " +
                "  if (element === document.body) return '/html/body'; " +
                "  var ix = 0; var siblings = element.parentNode.childNodes; " +
                "  for (var i = 0; i < siblings.length; i++) { " +
                "    var sibling = siblings[i]; " +
                "    if (sibling === element) { " +
                "      return getElementXPath(element.parentNode) + '/' + element.tagName.toLowerCase() + '[' + (ix + 1) + ']'; " +
                "    } " +
                "    if (sibling.nodeType === 1 && sibling.tagName === element.tagName) ix++; " +
                "  } " +
                "} " +
                "return getElementXPath(arguments[0]);", element.getWrappedElement());
        } catch (Exception e) {
            log.warn("Ошибка при получении XPath: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Получает все атрибуты элемента через JavaScript
     * 
     * @param element элемент для получения атрибутов
     * @param js JavaScript executor
     * @return Map атрибутов
     */
    @SuppressWarnings("unchecked")
    private static Map<String, String> getAllAttributes(SelenideElement element, PwDriver driver) {
        try {
            return (Map<String, String>) driver.executeScript(
                "var items = {}; " +
                "for (index = 0; index < arguments[0].attributes.length; ++index) { " +
                "  items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value " +
                "} " +
                "return items;", element.getWrappedElement());
        } catch (Exception e) {
            log.warn("Ошибка при получении атрибутов: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Сохраняет структуру формы в JSON файл
     * 
     * Сохраняет список метаданных полей формы в файл для дальнейшего использования
     * в отладке и создании тестов.
     * 
     * @param fields список метаданных полей
     * @param formName название формы (для имени файла)
     * @return путь к сохранённому файлу
     */
    public static String saveFormStructureToJson(List<FormFieldMetadata> fields, String formName) {
        try {
            File debugDir = new File("target/debug");
            if (!debugDir.exists()) {
                debugDir.mkdirs();
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = formName + "_structure_" + timestamp + ".json";
            File jsonFile = new File(debugDir, filename);
            
            try (FileWriter writer = new FileWriter(jsonFile)) {
                gson.toJson(fields, writer);
            }
            
            log.info("========================================");
            log.info("Структура формы сохранена в файл:");
            log.info(jsonFile.getAbsolutePath());
            log.info("Всего полей: " + fields.size());
            log.info("========================================");
            
            return jsonFile.getAbsolutePath();
            
        } catch (IOException e) {
            log.warn("Ошибка при сохранении структуры формы в JSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Сохраняет HTML формы в файл
     * 
     * Дополнительно сохраняет HTML для визуального анализа.
     * 
     * @param html HTML содержимое формы
     * @param formName название формы (для имени файла)
     * @return путь к сохранённому файлу
     */
    public static String saveHtmlToFile(String html, String formName) {
        try {
            File debugDir = new File("target/debug");
            if (!debugDir.exists()) {
                debugDir.mkdirs();
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = formName + "_" + timestamp + ".html";
            File htmlFile = new File(debugDir, filename);
            
            try (FileWriter writer = new FileWriter(htmlFile)) {
                writer.write(html != null ? html : "");
            }
            
            log.info("HTML сохранён в файл: " + htmlFile.getAbsolutePath());
            
            return htmlFile.getAbsolutePath();
            
        } catch (IOException e) {
            log.warn("Ошибка при сохранении HTML: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Парсит форму и сохраняет все данные (JSON + HTML)
     * 
     * Удобный метод для полного парсинга и сохранения структуры формы.
     * 
     * @param formElement элемент формы для парсинга
     * @param formName название формы
     * @return путь к сохранённому JSON файлу
     */
    public static String parseAndSaveForm(SelenideElement formElement, String formName) {
        // Сохраняем HTML
        String html = formElement.getAttribute("outerHTML");
        saveHtmlToFile(html, formName);
        
        // Парсим структуру
        List<FormFieldMetadata> fields = parseFormStructure(formElement);
        
        // Сохраняем JSON
        return saveFormStructureToJson(fields, formName);
    }
}
