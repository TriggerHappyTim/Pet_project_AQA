package com.bft.strategy;

import org.testng.asserts.SoftAssert;

import java.util.*;

/**
 * Менеджер стратегий тестирования
 * Управляет выбором и выполнением стратегий
 */
public class TestStrategyManager {

    private final Map<ExecutionStrategyType, TestExecutionStrategy<?>> executionStrategies;
    private final Map<DataPreparationStrategy.DataPreparationType, DataPreparationStrategy<?>> dataStrategies;
    private final Map<ValidationStrategy.ValidationType, ValidationStrategy<?>> validationStrategies;

    public TestStrategyManager() {
        this.executionStrategies = new HashMap<>();
        this.dataStrategies = new HashMap<>();
        this.validationStrategies = new HashMap<>();
    }

    /**
     * Регистрирует стратегию выполнения
     */
    public <T> void registerExecutionStrategy(TestExecutionStrategy<T> strategy) {
        executionStrategies.put(strategy.getType(), strategy);
    }

    /**
     * Регистрирует стратегию подготовки данных
     */
    public <T> void registerDataStrategy(DataPreparationStrategy<T> strategy) {
        dataStrategies.put(strategy.getType(), strategy);
    }

    /**
     * Регистрирует стратегию валидации
     */
    public <T> void registerValidationStrategy(ValidationStrategy<T> strategy) {
        validationStrategies.put(strategy.getType(), strategy);
    }

    /**
     * Выполняет тест с автоматическим выбором стратегии
     */
    public <T> TestExecutionStrategy.TestContext executeTest(String testName, SoftAssert softAssert) {
        TestExecutionStrategy.TestContext context = new TestExecutionStrategy.TestContext(testName);

        // Выбираем подходящую стратегию выполнения
        TestExecutionStrategy<T> executionStrategy = findBestExecutionStrategy(context);

        if (executionStrategy == null) {
            throw new RuntimeException("Не найдена подходящая стратегия выполнения для теста: " + testName);
        }

        try {
            // Выполняем тест по шагам
            executionStrategy.prepare(context);
            executionStrategy.execute(context);
            executionStrategy.validate(context, softAssert);
        } finally {
            executionStrategy.cleanup(context);
        }

        return context;
    }

    /**
     * Выполняет тест с указанной стратегией
     */
    public <T> TestExecutionStrategy.TestContext executeTestWithStrategy(
            String testName, TestStrategyType strategyType, SoftAssert softAssert) {

        @SuppressWarnings("unchecked")
        TestExecutionStrategy<T> strategy = (TestExecutionStrategy<T>) executionStrategies.get(strategyType);

        if (strategy == null) {
            throw new RuntimeException("Стратегия не зарегистрирована: " + strategyType);
        }

        TestExecutionStrategy.TestContext context = strategy.createContext();
        context.setTestName(testName);

        try {
            strategy.prepare(context);
            strategy.execute(context);
            strategy.validate(context, softAssert);
        } finally {
            strategy.cleanup(context);
        }

        return context;
    }

    /**
     * Выполняет тест с полной настройкой
     */
    public <T> TestExecutionStrategy.TestContext executeTestWithFullSetup(
            String testName,
            ExecutionStrategyType executionType,
            DataPreparationStrategy.DataPreparationType dataType,
            ValidationStrategy.ValidationType validationType,
            SoftAssert softAssert) {

        // Получаем стратегии
        @SuppressWarnings("unchecked")
        TestExecutionStrategy<T> executionStrategy = (TestExecutionStrategy<T>) executionStrategies.get(executionType);
        @SuppressWarnings("unchecked")
        DataPreparationStrategy<T> dataStrategy = (DataPreparationStrategy<T>) dataStrategies.get(dataType);
        @SuppressWarnings("unchecked")
        ValidationStrategy<T> validationStrategy = (ValidationStrategy<T>) validationStrategies.get(validationType);

        if (executionStrategy == null) {
            throw new RuntimeException("Стратегия выполнения не найдена: " + executionType);
        }

        TestExecutionStrategy.TestContext context = executionStrategy.createContext();
        context.setTestName(testName);

        T testData = null;

        try {
            // Подготовка данных
            if (dataStrategy != null) {
                testData = dataStrategy.createTestData();
                dataStrategy.prepareTestData(testData);
                context.setTestData(testData);
            }

            // Выполнение теста
            executionStrategy.prepare(context);
            executionStrategy.execute(context);

            // Валидация
            if (validationStrategy != null) {
                @SuppressWarnings("unchecked")
                T actualResult = (T) context.getActualResult();
                @SuppressWarnings("unchecked")
                T expectedResult = (T) context.getExpectedResult();
                validationStrategy.validate(actualResult, expectedResult, softAssert);
                validationStrategy.validatePerformance(context.getDuration(), softAssert);
            } else {
                executionStrategy.validate(context, softAssert);
            }

        } finally {
            // Очистка
            executionStrategy.cleanup(context);
            if (dataStrategy != null && testData != null) {
                dataStrategy.cleanupTestData(testData);
            }
        }

        return context;
    }

    /**
     * Возвращает список доступных стратегий выполнения
     */
    public List<TestStrategyType> getAvailableExecutionStrategies() {
        return new ArrayList<>(executionStrategies.keySet());
    }

    /**
     * Возвращает список доступных стратегий подготовки данных
     */
    public List<DataPreparationStrategy.DataPreparationType> getAvailableDataStrategies() {
        return new ArrayList<>(dataStrategies.keySet());
    }

    /**
     * Возвращает список доступных стратегий валидации
     */
    public List<ValidationStrategy.ValidationType> getAvailableValidationStrategies() {
        return new ArrayList<>(validationStrategies.keySet());
    }

    /**
     * Проверяет, зарегистрирована ли стратегия
     */
    public boolean isStrategyRegistered(ExecutionStrategyType type) {
        return executionStrategies.containsKey(type);
    }

    /**
     * Находит лучшую стратегию выполнения для контекста
     */
    @SuppressWarnings("unchecked")
    private <T> TestExecutionStrategy<T> findBestExecutionStrategy(TestExecutionStrategy.TestContext context) {
        return (TestExecutionStrategy<T>) executionStrategies.values().stream()
                .filter(strategy -> strategy.isApplicable(context))
                .max(Comparator.comparingInt(TestExecutionStrategy::getPriority))
                .orElse(null);
    }

    /**
     * Создает менеджер с предустановленными стратегиями
     */
    public static TestStrategyManager createDefault() {
        TestStrategyManager manager = new TestStrategyManager();

        // Регистрируем стандартные стратегии
        manager.registerExecutionStrategy(new CryptoProValidationStrategy());
        manager.registerExecutionStrategy(new ApiTestExecutionStrategy("/health", "GET", 200));

        return manager;
    }
}