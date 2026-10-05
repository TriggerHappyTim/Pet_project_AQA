package com.bft.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Telegram notifier for test results. Uses the plain Bot API over HTTP
 * (no polling, no bot framework) so it is safe to call from test threads.
 *
 * <p>Configuration (system property / env var):
 * <ul>
 *   <li>{@code telegram.bot-token} / {@code TELEGRAM_BOT_TOKEN}</li>
 *   <li>{@code telegram.chat-id} / {@code TELEGRAM_CHAT_ID}</li>
 *   <li>{@code telegram.per-test} / {@code TELEGRAM_PER_TEST} —
 *       send a message for EVERY test (default {@code false}: only summaries).
 *       Per-test messages on large suites hit Telegram rate limits quickly.</li>
 * </ul>
 *
 * <p>Built-in throttling: at most one message per ~1.1 s (Telegram allows
 * ~1 msg/sec to the same chat). Extra messages are dropped with a warning.
 */
public class TelegramNotifier {

    private static final Logger log = LoggerFactory.getLogger(TelegramNotifier.class);

    private static final String BOT_TOKEN_PROPERTY = "telegram.bot-token";
    private static final String CHAT_ID_PROPERTY = "telegram.chat-id";
    private static final String BOT_TOKEN_ENV = "TELEGRAM_BOT_TOKEN";
    private static final String CHAT_ID_ENV = "TELEGRAM_CHAT_ID";
    private static final long MIN_INTERVAL_MS = 1100;

    /** Shared across instances: Telegram limits are per-chat, not per-notifier. */
    private static volatile long lastSentAtMs = 0;

    private final String botToken;
    private final String chatId;
    private final boolean perTestMessages;
    private final boolean enabled;
    private final HttpClient http;

    public TelegramNotifier() {
        this.botToken = resolveProperty(BOT_TOKEN_PROPERTY, BOT_TOKEN_ENV);
        this.chatId = resolveProperty(CHAT_ID_PROPERTY, CHAT_ID_ENV);
        this.enabled = botToken != null && !botToken.isEmpty()
                && chatId != null && !chatId.isEmpty();
        this.perTestMessages = resolveBoolean("telegram.per-test", "TELEGRAM_PER_TEST", false);
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        if (enabled) {
            log.info("Telegram notifications enabled (perTest={}, chatId={})",
                    perTestMessages, chatId);
        } else {
            log.debug("Telegram notifications disabled (no bot-token/chat-id configured)");
        }
    }

    /**
     * Sends a single test result notification.
     *
     * @param testName fully qualified test name
     * @param passed   true if test passed
     * @param error    error message if failed (null for passed)
     */
    public void sendTestResult(String testName, boolean passed, String error) {
        if (!enabled || !perTestMessages) return;

        String emoji = passed ? "\u2705" : "\u274C";
        StringBuilder text = new StringBuilder();
        text.append(emoji).append(' ').append(passed ? "PASSED" : "FAILED").append('\n');
        text.append('`').append(escapeMarkdown(testName)).append('`');
        if (!passed && error != null) {
            text.append("\n\u26A0 ").append(truncate(error, 300));
        }
        sendMessageThrottled(text.toString());
    }

    /**
     * Sends a suite summary.
     *
     * @param total   total tests executed
     * @param passed  passed count
     * @param failed  failed count
     * @param skipped skipped count
     */
    public void sendSummary(int total, int passed, int failed, int skipped) {
        if (!enabled || total <= 0) return;

        StringBuilder text = new StringBuilder("\uD83D\uDCCA *Test Summary*\n");
        text.append("Total: `").append(total).append("`\n");
        text.append("\u2705 Passed: `").append(passed).append("`\n");
        if (failed > 0) text.append("\u274C Failed: `").append(failed).append("`\n");
        if (skipped > 0) text.append("\u23ED Skipped: `").append(skipped).append("`\n");

        double rate = total > 0 ? (double) passed / total * 100 : 0;
        text.append("Success: *").append(String.format("%.1f%%", rate)).append('*');

        sendMessageThrottled(text.toString());
    }

    /**
     * Sends a message bypassing the per-test switch (used for summaries).
     * Still respects throttling.
     */
    public void sendMessage(String markdownText) {
        if (!enabled) return;
        sendMessageThrottled(markdownText);
    }

    // ==================== Internals ====================

    private synchronized void sendMessageThrottled(String text) {
        long now = System.currentTimeMillis();
        long sinceLast = now - lastSentAtMs;
        if (sinceLast < MIN_INTERVAL_MS) {
            log.debug("Telegram: message dropped by throttle ({} ms since last)",
                    sinceLast);
            return;
        }
        lastSentAtMs = now;

        try {
            String payload = "{\"chat_id\":\"" + escapeJson(chatId) + "\","
                    + "\"text\":\"" + escapeJson(text) + "\","
                    + "\"parse_mode\":\"Markdown\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.telegram.org/bot" + botToken + "/sendMessage"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response =
                    http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                log.warn("Telegram API {}: {}", response.statusCode(),
                        truncate(response.body(), 200));
            } else {
                log.debug("Telegram message delivered to {}", chatId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Telegram send interrupted");
        } catch (Exception e) {
            log.warn("Failed to send Telegram message: {}", e.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isPerTestMessages() {
        return perTestMessages;
    }

    private String resolveProperty(String sysProp, String envVar) {
        String value = System.getProperty(sysProp);
        if (value != null && !value.isEmpty()) return value;
        value = System.getenv(envVar);
        if (value != null && !value.isEmpty()) return value;
        return null;
    }

    private boolean resolveBoolean(String sysProp, String envVar, boolean defaultValue) {
        String value = System.getProperty(sysProp);
        if (value == null || value.isEmpty()) value = System.getenv(envVar);
        if (value == null || value.isEmpty()) return defaultValue;
        try {
            return Boolean.parseBoolean(value.trim());
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_").replace("*", "\\*").replace("[", "\\[");
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
