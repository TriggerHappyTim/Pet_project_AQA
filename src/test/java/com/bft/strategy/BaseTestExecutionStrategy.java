package com.bft.strategy;

import org.testng.asserts.SoftAssert;

/**
 * Базовый класс для стратегий выполнения тестов
 * Предоставляет общую функциональность для всех стратегий
 */
public abstract class BaseTestExecutionStrategy<T> implements TestExecutionStrategy<T> {

    protected final String strategyName;
    protected final int priority;

    protected BaseTestExecutionStrategy(String strategyName, int priority) {
        this.strategyName = strategyName;
        this.priority = priority;
    }

    @Override
    public void prepare(TestContext context) {
        System.out.println("Подготовка теста '" + context.getTestName() + "' стратегией: " + strategyName);
        performPreparation(context);
    }

    @Override
    public void execute(TestContext context) {
        System.out.println("Выполнение теста '" + context.getTestName() + "' стратегией: " + strategyName);
        long startTime = System.currentTimeMillis();

        try {
            performExecution(context);
        } finally {
            context.setEndTime(System.currentTimeMillis());
            System.out.println("Тест '" + context.getTestName() + "' выполнен за " + context.getDuration() + " мс");
        }
    }

    @Override
    public void validate(TestContext context, SoftAssert softAssert) {
        System.out.println("Валидация результатов теста '" + context.getTestName() + "' стратегией: " + strategyName);
        performValidation(context, softAssert);
    }

    @Override
    public void cleanup(TestContext context) {
        System.out.println("Очистка после теста '" + context.getTestName() + "' стратегией: " + strategyName);
        performCleanup(context);
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
    protected abstract void performValidation(TestContext context, SoftAssert softAssert);

    /**
     * Выполняет специфическую очистку
     */
    protected abstract void performCleanup(TestContext context);
}