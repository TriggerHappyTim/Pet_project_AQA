# Performance Monitoring и Flaky Test Detection

Модуль для мониторинга производительности тестов и выявления нестабильных тестов.

## Классы

### PerformanceMonitor
Отслеживает время выполнения тестов и предоставляет статистику.

**Пример использования:**

```java
public class MyTest extends UITestBase {
    private static final PerformanceMonitor monitor = PerformanceMonitor.getInstance();
    
    @BeforeEach
    public void setup(TestInfo testInfo) {
        monitor.startTest(testInfo);
    }
    
    @AfterEach
    public void teardown(TestInfo testInfo) {
        monitor.endTest(testInfo);
    }
    
    @AfterAll
    public static void printStatistics() {
        monitor.printStatistics();
    }
}
```

**Функциональность:**
- Отслеживание времени выполнения каждого теста
- Определение медленных тестов (порог: 30 секунд)
- Статистика: среднее время, максимальное/минимальное время
- Топ-5 самых медленных тестов

### FlakyTestDetector
Выявляет нестабильные тесты, которые периодически падают без видимых причин.

**Пример использования:**

```java
public class MyTest extends UITestBase {
    private static final FlakyTestDetector detector = FlakyTestDetector.getInstance();
    
    @AfterEach
    public void trackTest(TestInfo testInfo) {
        detector.recordTestResult(testInfo);
    }
    
    @AfterAll
    public static void reportFlakyTests() {
        detector.reportFlakyTests();
    }
}
```

**Функциональность:**
- Отслеживание результатов выполнения тестов
- Определение нестабильных тестов (порог: 95% успешности, минимум 5 выполнений)
- Отслеживание типов ошибок для анализа причин нестабильности
- Детальная статистика по каждому нестабильному тесту

## Настройки

### PerformanceMonitor
- `SLOW_TEST_THRESHOLD_MS` - порог для определения медленных тестов (по умолчанию: 30 секунд)

### FlakyTestDetector
- `FLAKY_THRESHOLD` - порог успешности для определения нестабильного теста (по умолчанию: 0.95 = 95%)
- `MIN_EXECUTIONS` - минимальное количество выполнений для определения нестабильности (по умолчанию: 5)

## Интеграция с JUnit 5

Оба класса используют `TestInfo` из JUnit 5 для отслеживания тестов.
Уникальный идентификатор теста: `className#methodName`.

## Логирование

Все предупреждения и статистика выводятся в лог через SLF4J Logger.
