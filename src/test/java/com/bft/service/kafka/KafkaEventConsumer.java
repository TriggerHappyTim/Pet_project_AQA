package com.bft.service.kafka;

import io.qameta.allure.Allure;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Kafka consumer for verifying asynchronous events after UI/API actions.
 *
 * <p>Typical usage — after a UI action, wait for the system to emit the event:
 * <pre>{@code
 * try (KafkaEventConsumer kafka = new KafkaEventConsumer()) {
 *     Optional<KafkaMessage> event = kafka.waitForMessage(
 *         "ru.gov.evs.report.events",
 *         msg -> msg.value.contains("\"reportId\":\"" + reportId + "\""),
 *         Duration.ofSeconds(30));
 *     assertions.assertTrue(event.isPresent(), "Событие отчёта должно прийти в Kafka");
 * }
 * }</pre>
 *
 * <p>Or read recent history:
 * <pre>{@code
 * List<KafkaMessage> last = kafka.readRecent(topic, 20, Duration.ofSeconds(5));
 * }</pre>
 *
 * <p>Consumed messages are attached to Allure for reporting.
 */
public class KafkaEventConsumer implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);

    private final KafkaConsumer<String, String> consumer;
    private final long pollIntervalMs;

    public KafkaEventConsumer() {
        this(KafkaConfig.getInstance());
    }

    KafkaEventConsumer(KafkaConfig config) {
        this.pollIntervalMs = config.getPollIntervalMs();
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        // Unique group id: every run gets a fresh consumer group (no committed offsets)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "autotest-" + UUID.randomUUID());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        this.consumer = new KafkaConsumer<>(props);
    }

    /**
     * A single consumed message.
     */
    public static class KafkaMessage {
        public final String topic;
        public final String key;
        public final String value;
        public final long timestamp;
        public final long offset;
        public final int partition;

        KafkaMessage(ConsumerRecord<String, String> record) {
            this.topic = record.topic();
            this.key = record.key();
            this.value = record.value();
            this.timestamp = record.timestamp();
            this.offset = record.offset();
            this.partition = record.partition();
        }

        @Override
        public String toString() {
            return String.format("[%s p%d @%d] %s", topic, partition, offset,
                    value == null ? "null" : truncate(value));
        }

        private String truncate(String s) {
            return s.length() <= 200 ? s : s.substring(0, 200) + "...";
        }
    }

    // ==================== Public API ====================

    /**
     * Waits until a message matching {@code matcher} appears on the topic.
     * Only NEW messages (published after subscription) are inspected.
     *
     * @param topic          topic to watch
     * @param matcher        predicate to match the desired message
     * @param timeout        total wait time
     * @return matched message or empty on timeout
     */
    public Optional<KafkaMessage> waitForMessage(String topic,
                                                 Predicate<KafkaMessage> matcher,
                                                 Duration timeout) {
        return waitForMessage(List.of(topic), matcher, timeout);
    }

    /**
     * Waits for a matching message across several topics.
     */
    public Optional<KafkaMessage> waitForMessage(List<String> topics,
                                                 Predicate<KafkaMessage> matcher,
                                                 Duration timeout) {
        log.info("Kafka: waiting for message in {} (timeout {})", topics, timeout);
        long deadline = System.currentTimeMillis() + timeout.toMillis();

        consumer.subscribe(topics);
        // Ждём фактического назначения партиций: poll(ZERO) может завершиться ДО
        // завершения rebalance, и seekToEnd по пустому assignment — no-op.
        // Бездействуем не дольше 5 секунд; с auto.offset.reset=latest свежая группа
        // и так стартует с конца, это лишь страховка от гонки.
        long assignDeadline = System.currentTimeMillis() + 5000;
        while (consumer.assignment().isEmpty() && System.currentTimeMillis() < assignDeadline) {
            consumer.poll(Duration.ofMillis(100));
        }
        if (!consumer.assignment().isEmpty()) {
            consumer.seekToEnd(consumer.assignment());
        } else {
            log.warn("Kafka: partition assignment not received in 5s, "
                    + "relying on auto.offset.reset=latest");
        }

        List<KafkaMessage> seen = new ArrayList<>();
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(pollIntervalMs));
            for (ConsumerRecord<String, String> record : records) {
                KafkaMessage message = new KafkaMessage(record);
                seen.add(message);
                if (matcher.test(message)) {
                    attachToAllure("Kafka matched message", seen);
                    log.info("Kafka: matched {} message(s), latest: {}", seen.size(), message);
                    return Optional.of(message);
                }
            }
        }

        attachToAllure("Kafka messages seen before timeout", seen);
        log.warn("Kafka: no matching message in {} within {} (seen {})",
                topics, timeout, seen.size());
        return Optional.empty();
    }

    /**
     * Reads up to {@code maxMessages} most recent records per partition without waiting
     * for new events. Useful for post-action verification against history.
     */
    public List<KafkaMessage> readRecent(String topic, int maxPerPartition, Duration maxWait) {
        List<KafkaMessage> result = new ArrayList<>();
        consumer.subscribe(List.of(topic));
        consumer.poll(Duration.ofSeconds(2)); // trigger partition assignment

        var assignment = consumer.assignment();
        if (assignment.isEmpty()) {
            log.warn("Kafka: no partitions assigned for '{}'", topic);
            return result;
        }

        Map<TopicPartition, Long> endOffsets = consumer.endOffsets(assignment);
        for (TopicPartition partition : assignment) {
            long end = endOffsets.get(partition);
            long start = Math.max(end - maxPerPartition, 0);
            consumer.seek(partition, start);
        }

        long deadline = System.currentTimeMillis() + maxWait.toMillis();
        while (System.currentTimeMillis() < deadline && !Thread.currentThread().isInterrupted()) {
            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(pollIntervalMs));
            for (ConsumerRecord<String, String> record : records) {
                result.add(new KafkaMessage(record));
            }
            boolean complete = result.size() >= maxPerPartition * Math.max(assignment.size(), 1)
                    || records.isEmpty();
            if (complete) {
                break;
            }
        }

        attachToAllure("Kafka recent messages (" + topic + ")", result);
        log.info("Kafka: read {} recent message(s) from '{}'", result.size(), topic);
        return result;
    }

    @Override
    public void close() {
        try {
            consumer.close(Duration.ofSeconds(5));
        } catch (Exception e) {
            log.debug("Kafka: close error (ignored): {}", e.getMessage());
        }
    }

    // ==================== Helpers ====================

    private void attachToAllure(String title, List<KafkaMessage> messages) {
        try {
            StringBuilder text = new StringBuilder("Count: ").append(messages.size()).append('\n');
            for (int i = Math.max(0, messages.size() - 20); i < messages.size(); i++) {
                text.append(messages.get(i)).append('\n');
            }
            Allure.addAttachment(title, "text/plain", text.toString(), ".txt");
        } catch (Exception e) {
            log.debug("Kafka: failed to attach to Allure: {}", e.getMessage());
        }
    }
}
