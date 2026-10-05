package com.bft.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bft.test.TestAssertions;

/**
 * Базовый класс для стратегий выполнения тестов
 * Предоставляет общую функциональность для всех стратегий
 */
public abstract class BaseTestExecutionStrategy<T> implements TestExecutionStrategy<T> {

    protected static final Logger logger = LoggerFactory.getLogger(BaseTestExecutionStrategy.class);
    
    protected final String strategyName;
    protected final int priority;

    protected BaseTestExecutionStrategy(String strategyName, int priority) {
        this.strategyName = strategyName;
        this.priority = priority;
    }

    @Override
    public void prepare(TestContext context) {
        logger.info("Подготовка теста '{}' стратегией: {}", context.getTestName(), strategyName);
        try {
            performPreparation(context);
            logger.debug("Подготовка теста '{}' завершена успешно", context.getTestName());
        } catch (Exception e) {
            logger.error("Ошибка при подготовке теста '{}': {}", context.getTestName(), e.getMessage(), e);
            context.setTestData("PREPARATION_FAILED: " + e.getMessage());
            throw new RuntimeException("Ошибка подготовки теста: " + e.getMessage(), e);
        }
    }

    @Override
    public void execute(TestContext context) {
        logger.info("Выполнение теста '{}' стратегией: {}", context.getTestName(), strategyName);
        long startTime = System.currentTimeMillis();
        context.setStartTime(startTime);

        try {
            performExecution(context);
            logger.debug("Выполнение теста '{}' завершено успешно", context.getTestName());
        } catch (Exception e) {
            logger.error("Ошибка при выполнении теста '{}': {}", context.getTestName(), e.getMessage(), e);
            context.setActualResult("EXECUTION_FAILED: " + e.getMessage());
            throw new RuntimeException("Ошибка выполнения теста: " + e.getMessage(), e);
        } finally {
            long endTime = System.currentTimeMillis();
            context.setEndTime(endTime);
            long duration = endTime - startTime;
            logger.info("Тест '{}' выполнен за {} мс", context.getTestName(), duration);
        }
    }

    @Override
    public void validate(TestContext context, TestAssertions softAssert) {
        logger.info("Валидация результатов теста '{}' стратегией: {}", context.getTestName(), strategyName);
        try {
            performValidation(context, softAssert);
            logger.debug("Валидация теста '{}' завершена успешно", context.getTestName());
        } catch (Exception e) {
            logger.error("Ошибка при валидации теста '{}': {}", context.getTestName(), e.getMessage(), e);
            if (softAssert != null) {
                softAssert.fail("Ошибка валидации: " + e.getMessage());
            }
            throw new RuntimeException("Ошибка валидации теста: " + e.getMessage(), e);
        }
    }

    @Override
    public void cleanup(TestContext context) {
        logger.info("Очистка после теста '{}' стратегией: {}", context.getTestName(), strategyName);
        try {
            performCleanup(context);
            logger.debug("Очистка теста '{}' завершена успешно", context.getTestName());
        } catch (Exception e) {
            // Логируем ошибки cleanup, но не прерываем выполнение
            logger.warn("Ошибка при очистке теста '{}': {}", context.getTestName(), e.getMessage(), e);
            // Не пробрасываем исключение, чтобы не скрыть основную ошибку теста
        }
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public TestContext createContext() {
        return new TestContext(strategyName + "_" + System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return strategyName + " (priority: " + priority + ")";
    }

    /**
     * Выполняет специфическую подготовку для стратегии
     */
    protected abstract void performPreparation(TestContext context);

    /**
     * Выполняет специфическую логику выполнения
     */
    protected abstract void performExecution(TestContext context);

    /**
     * Выполняет специфическую валидацию
     */
    protected abstract void performValidation(TestContext context, TestAssertions softAssert);

    /**
     * Выполняет специфическую очистку
     */
    protected abstract void performCleanup(TestContext context);
}