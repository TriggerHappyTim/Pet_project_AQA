// ============================================================================
// ЗАКОММЕНТИРОВАНО: Zephyr/Jira интеграция не используется в проекте
// ============================================================================
// package com.bft.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для связи теста с test case в Zephyr
 * 
 * <p>Используется для автоматической публикации результатов тестов в Zephyr.
 * Может быть применена к классу (для всех тестов в классе) или к отдельному методу.
 * 
 * <p><b>Пример использования:</b>
 * <pre>
 * {@code
 * @Feature("User Authentication")
 * @ZephyrTest(testKey = "EVS-T-101", testCycle = "Sprint-23")
 * public class LoginTest extends UITestBase {
 *     
 *     @Test(groups = {"web", "smoke"})
 *     @ZephyrTest(testKey = "EVS-T-102")
 *     public void successfulLogin() {
 *         // Test автоматически отправится в Zephyr после выполнения
 *     }
 * }
 * }
 * </pre>
 * 
 * @see com.bft.integration.listeners.ZephyrTestListener
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ZephyrTest {
    
    /**
     * Ключ test case в Zephyr (например, "EVS-T-123")
     * 
     * @return ключ test case
     */
    String testKey();
    
    /**
     * Ключ test cycle в Zephyr (опционально)
     * Если не указан, используется значение из конфигурации
     * 
     * @return ключ test cycle
     */
    String testCycle() default "";
    
    /**
     * Создавать ли test case в Zephyr, если он не существует
     * 
     * @return true, если нужно создать test case
     */
    boolean createIfNotExists() default false;
    
    /**
     * Описание теста для Zephyr (опционально)
     * 
     * @return описание теста
     */
    String description() default "";
    
    /**
     * Приоритет теста в Zephyr (опционально)
     * Возможные значения: "High", "Medium", "Low"
     * 
     * @return приоритет теста
     */
    String priority() default "";
// }
