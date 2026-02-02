package com.bft.integration.mapper;

/**
 * Статус выполнения теста
 */
public enum TestStatus {
    /**
     * Тест пройден успешно
     */
    PASS("PASS", "Passed"),
    
    /**
     * Тест провален
     */
    FAIL("FAIL", "Failed"),
    
    /**
     * Тест пропущен
     */
    SKIP("SKIP", "Skipped"),
    
    /**
     * Тест заблокирован
     */
    BLOCKED("BLOCKED", "Blocked"),
    
    /**
     * Тест в процессе выполнения
     */
    IN_PROGRESS("IN_PROGRESS", "In Progress"),
    
    /**
     * Тест не выполнен
     */
    NOT_EXECUTED("NOT_EXECUTED", "Not Executed");
    
    private final String code;
    private final String displayName;
    
    TestStatus(String code, String displayName) {
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
     * Маппинг из TestNG статуса
     * 
     * @param testNGStatus статус из ITestResult
     * @return соответствующий TestStatus
     */
    public static TestStatus fromTestNGStatus(int testNGStatus) {
        switch (testNGStatus) {
            case 1: // SUCCESS
                return PASS;
            case 2: // FAILURE
                return FAIL;
            case 3: // SKIP
                return SKIP;
            default:
                return NOT_EXECUTED;
        }
    }
}
