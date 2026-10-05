package com.bft.test.examples;

import com.bft.service.kafka.KafkaEventConsumer;
import com.bft.service.kafka.KafkaEventConsumer.KafkaMessage;
import com.bft.test.base.UITestBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

/**
 * ============================== УЧЕБНЫЙ ПРИМЕР №5 ==============================
 *
 * Проверка асинхронных событий в Kafka после действий через UI.
 *
 * ЗАЧЕМ ЭТО НУЖНО:
 *   Многие процессы в ЕВС асинхронные: вы нажали кнопку, а система «в фоне»
 *   отправила событие в Kafka, которое подхватят другие сервисы. Если событие
 *   НЕ ушло — бизнес-процесс встал, хотя UI показывает успех. Этот пример
 *   позволяет дождаться события и убедиться, что оно корректное.
 *
 * КАК ЧИТАТЬ ТАКОЙ ТЕСТ (сценарий):
 *   1. Выполняем действие через UI (например, подписываем документ).
 *   2. ОТКРЫВАЕМ слушателя Kafka ДО или сразу после действия.
 *   3. Ждём сообщение, подходящее под условие (не дольше таймаута).
 *   4. Проверяем содержимое сообщения.
 *
 * КАК НАСТРОИТЬ:
 *   -Dkafka.servers=172.18.38.9:9092          (контур INT)
 *   -Dkafka.servers=localhost:29093           (локальный docker)
 * ==============================================================================
 */
@Disabled("Обучающий пример №5 — не запускать в CI")
@Tag("example")
@Tag("kafka")
public class Example05_KafkaEventsTest extends UITestBase {

    @Test
    public void signingEmitsKafkaEvent() {
        // Замените на реальный топик вашего процесса. Имена топиков обычно длинные
        // и говорящие, например: ru.gov.evs.report.events.v1
        String topic = "evs.report.events";

        // ---------- 1. Действие через UI ----------
        // steps.signRequest();  // ← здесь могло быть ваше действие

        // ---------- 2. Ждём событие ----------
        // try-with-resources ГАРАНТИРУЕТ закрытие соединения с Kafka,
        // даже если тест упадёт посередине. Всегда используйте такую форму!
        try (KafkaEventConsumer kafka = new KafkaEventConsumer()) {

            Optional<KafkaMessage> event = kafka.waitForMessage(
                    topic,
                    // Условие поиска сообщения. Сюда приходит каждое новое сообщение
                    // из топика; возвращайте true, когда нашли нужное.
                    msg -> msg.value != null && msg.value.contains("\"status\":\"SIGNED\""),
                    // Таймаут: сколько ждать максимум. Если за это время событие
                    // не пришло — метод вернёт empty, тест продолжится и упадёт
                    // на проверке ниже (с полным списком виденных сообщений в Allure).
                    Duration.ofSeconds(30));

            // ---------- 3. Проверки ----------
            assertions.assertTrue(event.isPresent(),
                    "Событие о подписании должно прийти в Kafka за 30 секунд");

            if (event.isPresent()) {
                assertions.assertTrue(event.get().value.contains("\"reportId\""),
                        "Событие должно содержать идентификатор отчёта");
            }
        }
    }

    /**
     * Альтернативный режим: прочитать ПОСЛЕДНИЕ N сообщений из топика
     * (историю), а не ждать новые. Полезно, когда событие уже случилось.
     */
    @Test
    public void readRecentMessagesExample() {
        try (KafkaEventConsumer kafka = new KafkaEventConsumer()) {
            var recent = kafka.readRecent("evs.report.events", 20, Duration.ofSeconds(5));
            logger.info("Прочитано {} последних сообщений", recent.size());

            assertions.assertFalse(recent.isEmpty(), "В топике должны быть сообщения");
        }
    }
}
