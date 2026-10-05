package com.bft.test.examples;

import com.bft.service.db.DbClient;
import com.bft.test.base.BaseTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * ============================== УЧЕБНЫЙ ПРИМЕР №4 ==============================
 *
 * Проверка данных напрямую в базе данных (Apache Phoenix / HBase).
 *
 * ЗАЧЕМ ЭТО НУЖНО:
 *   UI говорит «отчёт сохранён», но сохранился ли он РЕАЛЬНО? Прямой запрос в БД —
 *   самый надёжный способ убедиться, что данные записались корректно.
 *
 * ВАЖНО ПРО ТЕХНОЛОГИЮ:
 *   БД проекта EVS — это НЕ PostgreSQL, а Apache Phoenix (SQL-надстройка над HBase).
 *   Подключение идёт через Phoenix Query Server (порт 8765). Поэтому SQL немного
 *   отличается от классического: например, вместо AUTO_INCREMENT используются
 *   последовательности, а имена таблиц/колонок обычно ВЕРХНИМ регистром.
 *
 * КАК НАСТРОИТЬ ПОДКЛЮЧЕНИЕ:
 *   mvn test -Dtest=Example04_DatabaseCheckTest \
 *       -Ddb.url="jdbc:phoenix:thin:url=http://172.18.36.33:8765;serialization=PROTOBUF"
 *
 *   URL-ы по контурам (они же лежат в application-dev.yml / application-int.yml):
 *     dev/int: jdbc:phoenix:thin:url=http://172.18.36.33:8765;serialization=PROTOBUF
 *
 * ЕСЛИ URL НЕ ЗАДАН:
 *   Любой запрос бросит понятное исключение DbQueryException с подсказкой,
 *   какое свойство установить. Тест не зависнет и не упадёт с загадочной ошибкой.
 * ==============================================================================
 */
@Disabled("Обучающий пример №4 — не запускать в CI")
@Tag("example")
@Tag("db")
public class Example04_DatabaseCheckTest extends BaseTest {
    // ↑ Обратите внимание: extends BaseTest (а не UITestBase) — браузер для этого
    //   теста не нужен, значит и открывать его не надо. Тест выполнится быстрее.

    /** Клиент БД создаётся один раз на класс. */
    private final DbClient db = new DbClient();

    @Test
    public void reportIsReallySavedInDatabase() {

        // ---------- Пример 1. Проверка количества строк ----------
        // queryCount() принимает часть запроса после SELECT COUNT(*):
        long reportsCount = db.queryCount(
                "FROM REPORTS WHERE SNILS = ?",     // ← SQL с плейсхолдером ?
                "351-818-056-74");                  // ← значение подставляется БЕЗОПАСНО

        // Мягкая проверка: если отчётов не 1 — тест упадёт в конце с этим сообщением.
        assertions.assertEquals(1L, reportsCount,
                "В БД должен быть ровно один отчёт по этому СНИЛС");

        // ---------- Пример 2. Чтение конкретной строки ----------
        // queryRow() вернёт Map<String, Object> — «имя колонки -> значение».
        // Ключи совпадают с именами колонок в SELECT.
        java.util.Map<String, Object> request = db.queryRow(
                "SELECT STATUS, NUMBER FROM REQUESTS WHERE NUMBER = ?",
                "2026-001");

        assertions.assertNotNull(request, "Запрос с номером 2026-001 должен существовать");
        if (request != null) {
            assertions.assertEquals("SIGNED", String.valueOf(request.get("STATUS")),
                    "После подписания статус в БД должен стать SIGNED");
        }

        // ---------- Пример 3. Проверка существования ----------
        // rowExists() — короткая запись для «есть ли хоть одна строка».
        boolean hasAuditLog = db.rowExists(
                "SELECT 1 FROM AUDIT_LOG WHERE ENTITY_ID = ?", "12345");
        assertions.assertTrue(hasAuditLog, "Должна быть запись в журнале аудита");

        // ---------- Пример 4. Скалярное значение ----------
        Long maxId = db.queryScalar("SELECT MAX(ID) FROM REQUESTS");
        logger.info("Максимальный ID запроса в БД: {}", maxId);
    }

    /**
     * ПРАВИЛА БЕЗОПАСНОСТИ (обязательные!):
     *   1. НИКОГДА не склеивайте SQL строками: "... WHERE id = " + userId — это путь
     *      к SQL-инъекциям. Только плейсхолдеры "?".
     *   2. Не используйте DELETE/UPDATE против общих стендов без согласования —
     *      вы можете сломать данные другим тестировщикам.
     *   3. Каждый запрос и его результат автоматически прикрепляются к Allure —
     *      при падении видно, какой именно SQL выполнялся.
     */
}
