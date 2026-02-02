package com.bft.strategy;

import org.testng.asserts.SoftAssert;

/**
 * Стратегия выполнения теста
 * Определяет, как должен выполняться конкретный тестовый сценарий
 */
public interface TestExecutionStrategy<T> {

    /**
     * Выполняет подготовку перед тестом
     */
    void prepare(TestContext context);

    /**
     * Выполняет основной сценарий теста
     */
    void execute(TestContext context);

    /**
     * Выполняет проверки после выполнения
     */
    void validate(TestContext context, SoftAssert softAssert);

    /**
     * Выполняет очистку после теста
     */
    void cleanup(TestContext context);

    /**
     * Возвращает тип стратегии
     */
    ExecutionStrategyType getType();

    /**
     * Возвращает приоритет стратегии
     */
    int getPriority();

    /**
     * Проверяет, подходит ли стратегия для данного контекста
     */
    boolean isApplicable(TestContext context);

    /**
     * Создает контекст для стратегии
     */
    TestContext createContext();

    /**
     * Контекст выполнения стратегии
     */
    class TestContext {
        private String testName;
        private Object testData;
        private Object expectedResult;
        private Object actualResult;
        private long startTime;
        private long endTime;

        public TestContext(String testName) {
            this.testName = testName;
            this.startTime = System.currentTimeMillis();
        }

        // Getters and setters
        public String getTestName() { return testName; }
        public void setTestName(String testName) { this.testName = testName; }

        public Object getTestData() { return testData; }
        public void setTestData(Object testData) { this.testData = testData; }

        public Object getExpectedResult() { return expectedResult; }
        public void setExpectedResult(Object expectedResult) { this.expectedResult = expectedResult; }

        public Object getActualResult() { return actualResult; }
        public void setActualResult(Object actualResult) { this.actualResult = actualResult; }

        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }

        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public long getDuration() {
            return endTime > 0 ? endTime - startTime : System.currentTimeMillis() - startTime;
        }

        public void markCompleted() {
            this.endTime = System.currentTimeMillis();
        }
    }
}