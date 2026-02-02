package com.bft.integration.allure;

import java.util.List;

/**
 * DTO для результата теста из Allure JSON
 * 
 * <p>Упрощенная структура для парсинга allure-results/*.json файлов
 */
public class AllureTestResult {
    
    private String uuid;
    private String name;
    private String status;
    private Long start;
    private Long stop;
    private String statusDetails;
    private List<Label> labels;
    
    public static class Label {
        private String name;
        private String value;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
    }
    
    // Getters and Setters
    
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Long getStart() {
        return start;
    }
    
    public void setStart(Long start) {
        this.start = start;
    }
    
    public Long getStop() {
        return stop;
    }
    
    public void setStop(Long stop) {
        this.stop = stop;
    }
    
    public String getStatusDetails() {
        return statusDetails;
    }
    
    public void setStatusDetails(String statusDetails) {
        this.statusDetails = statusDetails;
    }
    
    public List<Label> getLabels() {
        return labels;
    }
    
    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }
    
    /**
     * Получить значение label по имени
     * 
     * @param labelName имя label
     * @return значение или null
     */
    public String getLabelValue(String labelName) {
        if (labels == null) return null;
        
        return labels.stream()
            .filter(l -> labelName.equals(l.getName()))
            .map(Label::getValue)
            .findFirst()
            .orElse(null);
    }
}
