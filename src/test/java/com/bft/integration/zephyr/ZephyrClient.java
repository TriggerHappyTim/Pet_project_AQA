// ============================================================================
// ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется в проекте
// ============================================================================
// package com.bft.integration.zephyr;

import com.bft.integration.mapper.TestResult;
import com.bft.integration.mapper.TestStatus;

import java.util.List;

/**
 * Интерфейс для работы с Zephyr API
 * 
 * <p>Поддерживает различные типы Zephyr:
 * <ul>
 *   <li>Zephyr Scale (REST API v2)</li>
 *   <li>Zephyr Squad (ZAPI)</li>
 * </ul>
 */
public interface ZephyrClient extends AutoCloseable {
    
    /**
     * Опубликовать результаты тестов в Zephyr
     * 
     * @param results список результатов тестов
     */
    void publishTestResults(List<TestResult> results);
    
    /**
     * Создать test cycle в Zephyr
     * 
     * @param name название cycle
     * @param description описание
     * @return ID созданного cycle
     */
    String createTestCycle(String name, String description);
    
    /**
     * Обновить test execution в Zephyr
     * 
     * @param testKey ключ test case
     * @param status статус выполнения
     * @param comment комментарий
     */
    void updateTestExecution(String testKey, TestStatus status, String comment);
    
    /**
     * Проверить доступность Zephyr API
     * 
     * @return true если API доступен
     */
    boolean isAvailable();
// }
