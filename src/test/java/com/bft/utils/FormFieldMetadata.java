package com.bft.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Метаданные поля формы для парсинга и отладки
 * 
 * Хранит информацию о поле формы: метки, селекторы, атрибуты,
 * позицию в DOM и другую полезную информацию для отладки и создания тестов.
 * 
 * @author QA Automation Team
 * @version 1.0
 */
public class FormFieldMetadata {
    
    private String fieldType; // input, select, button, textarea, combobox
    private String labelText; // Текст метки поля
    private List<String> labelVariants; // Варианты текста метки
    private String inputName; // Атрибут name
    private String inputId; // Атрибут id
    private String xpath; // XPath до элемента
    private Map<String, String> attributes; // Все атрибуты элемента
    private String cssClasses; // CSS классы
    private boolean isVisible; // Видимость элемента
    private boolean isEnabled; // Доступность элемента
    private String role; // ARIA role
    private String placeholder; // Placeholder текст
    private String currentValue; // Текущее значение
    
    /**
     * Конструктор по умолчанию
     */
    public FormFieldMetadata() {
        this.labelVariants = new ArrayList<>();
        this.attributes = new HashMap<>();
    }
    
    // Getters and Setters
    
    public String getFieldType() {
        return fieldType;
    }
    
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }
    
    public String getLabelText() {
        return labelText;
    }
    
    public void setLabelText(String labelText) {
        this.labelText = labelText;
    }
    
    public List<String> getLabelVariants() {
        return labelVariants;
    }
    
    public void setLabelVariants(List<String> labelVariants) {
        this.labelVariants = labelVariants;
    }
    
    public void addLabelVariant(String variant) {
        if (variant != null && !variant.isEmpty()) {
            this.labelVariants.add(variant);
        }
    }
    
    public String getInputName() {
        return inputName;
    }
    
    public void setInputName(String inputName) {
        this.inputName = inputName;
    }
    
    public String getInputId() {
        return inputId;
    }
    
    public void setInputId(String inputId) {
        this.inputId = inputId;
    }
    
    public String getXpath() {
        return xpath;
    }
    
    public void setXpath(String xpath) {
        this.xpath = xpath;
    }
    
    public Map<String, String> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes != null ? attributes : new HashMap<>();
    }
    
    public void addAttribute(String key, String value) {
        if (key != null && value != null) {
            this.attributes.put(key, value);
        }
    }
    
    public String getCssClasses() {
        return cssClasses;
    }
    
    public void setCssClasses(String cssClasses) {
        this.cssClasses = cssClasses;
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public void setVisible(boolean visible) {
        isVisible = visible;
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getPlaceholder() {
        return placeholder;
    }
    
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }
    
    public String getCurrentValue() {
        return currentValue;
    }
    
    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }
    
    @Override
    public String toString() {
        return String.format("FormFieldMetadata{type='%s', label='%s', name='%s', id='%s', xpath='%s'}",
            fieldType, labelText, inputName, inputId, xpath);
    }
}
