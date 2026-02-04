package com.bft.test.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для настройки механизма повторных попыток выполнения тестов
 * 
 * <p>Используется для указания максимального количества попыток выполнения
 * flaky тестов. Применяется вместе с {@link RetryAnalyzer}.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * @Test(retryAnalyzer = RetryAnalyzer.class, groups = {"web", "crypto"})
 * @Retry(maxAttempts = 5)
 * public void verifyCryptoProPluginLoaded() {
 *     // тест, который может быть нестабильным
 * }
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 1.0
 * @see RetryAnalyzer для реализации механизма retry
 * @since 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {
    
    /**
     * Максимальное количество попыток выполнения теста
     * 
     * Включает первоначальную попытку, так что при maxAttempts = 3
     * тест будет выполнен максимум 3 раза (1 первоначальная + 2 повтора).
     * 
     * @return максимальное количество попыток (по умолчанию 3)
     */
    int maxAttempts() default 3;
    
    /**
     * Причина использования retry для этого теста
     * 
     * Помогает документировать, почему тест помечен как flaky.
     * 
     * @return причина использования retry
     */
    String reason() default "";
}
