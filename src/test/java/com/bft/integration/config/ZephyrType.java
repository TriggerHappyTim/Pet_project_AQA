// ============================================================================
// ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется в проекте
// ============================================================================
// package com.bft.integration.config;

/**
 * Тип Zephyr для интеграции
 */
// public enum ZephyrType {
    /**
     * Zephyr Scale (бывший Zephyr for Jira)
     * Использует REST API v2
     */
    SCALE("scale", "Zephyr Scale"),
    
    /**
     * Zephyr Squad (встроенный в Jira)
     * Использует ZAPI
     */
    SQUAD("squad", "Zephyr Squad"),
    
    /**
     * Zephyr Standalone
     */
    STANDALONE("standalone", "Zephyr Standalone");
    
    private final String code;
    private final String displayName;
    
    ZephyrType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Получить тип Zephyr по коду
     * 
     * @param code код типа
     * @return тип Zephyr
     * @throws IllegalArgumentException если код неизвестен
     */
    public static ZephyrType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return SCALE; // default
        }
        
        for (ZephyrType type : values()) {
            if (type.code.equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("Unknown Zephyr type: " + code);
    }
// }
