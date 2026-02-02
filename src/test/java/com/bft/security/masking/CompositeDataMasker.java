package com.bft.security.masking;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Композитный маскировщик данных
 * Применяет несколько стратегий маскировки последовательно
 */
public class CompositeDataMasker implements DataMasker {

    private final List<DataMasker> maskers;

    public CompositeDataMasker() {
        maskers = new ArrayList<>();
        initializeDefaultMaskers();
    }

    private void initializeDefaultMaskers() {
        maskers.add(new PasswordMasker());
        maskers.add(new EmailMasker());
        maskers.add(new ApiKeyMasker());
        maskers.add(new JwtTokenMasker());
        maskers.add(new CreditCardMasker());
    }

    @Override
    public String mask(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        String result = input;
        for (DataMasker masker : maskers) {
            if (masker.containsSensitiveData(result)) {
                result = masker.mask(result);
            }
        }

        return result;
    }

    @Override
    public boolean containsSensitiveData(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        return maskers.stream().anyMatch(masker -> masker.containsSensitiveData(input));
    }

    @Override
    public MaskerType getType() {
        return MaskerType.GENERIC;
    }

    /**
     * Добавить дополнительный маскировщик
     */
    public void addMasker(DataMasker masker) {
        if (masker != null) {
            maskers.add(masker);
        }
    }

    /**
     * Удалить маскировщик
     */
    public void removeMasker(DataMasker masker) {
        maskers.remove(masker);
    }

    /**
     * Очистить все маскировщики
     */
    public void clearMaskers() {
        maskers.clear();
    }

    /**
     * Маскировщик паролей
     */
    private static class PasswordMasker implements DataMasker {
        private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "(?i)(password|pwd|pass|пароль)\\\\s*[=:]\\\\s*['\\\"]?([^'\\\"\\\\s]{3,})['\\\"]?",
            Pattern.CASE_INSENSITIVE);

        @Override
        public String mask(String input) {
            return PASSWORD_PATTERN.matcher(input)
                .replaceAll("$1: ***");
        }

        @Override
        public boolean containsSensitiveData(String input) {
            return PASSWORD_PATTERN.matcher(input).find();
        }

        @Override
        public MaskerType getType() {
            return MaskerType.PASSWORD;
        }
    }

    /**
     * Маскировщик email адресов
     */
    private static class EmailMasker implements DataMasker {
        private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

        @Override
        public String mask(String input) {
            return EMAIL_PATTERN.matcher(input)
                .replaceAll("***@" + "$1".substring("$1".lastIndexOf('.') + 1));
        }

        @Override
        public boolean containsSensitiveData(String input) {
            return EMAIL_PATTERN.matcher(input).find();
        }

        @Override
        public MaskerType getType() {
            return MaskerType.EMAIL;
        }
    }

    /**
     * Маскировщик API ключей
     */
    private static class ApiKeyMasker implements DataMasker {
        private static final Pattern API_KEY_PATTERN = Pattern.compile(
            "(?i)(api[_-]?key|apikey|secret|token|auth[_-]?key)\\\\s*[=:]\\\\s*['\\\"]?([a-zA-Z0-9_\\\\-]{10,})['\\\"]?",
            Pattern.CASE_INSENSITIVE);

        @Override
        public String mask(String input) {
            return API_KEY_PATTERN.matcher(input)
                .replaceAll("$1: ***" + ("$2".length() > 4 ? "$2".substring("$2".length() - 4) : ""));
        }

        @Override
        public boolean containsSensitiveData(String input) {
            return API_KEY_PATTERN.matcher(input).find();
        }

        @Override
        public MaskerType getType() {
            return MaskerType.API_KEY;
        }
    }

    /**
     * Маскировщик JWT токенов
     */
    private static class JwtTokenMasker implements DataMasker {
        private static final Pattern JWT_PATTERN = Pattern.compile(
            "eyJ[A-Za-z0-9-_]+\\.eyJ[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+");

        @Override
        public String mask(String input) {
            return JWT_PATTERN.matcher(input)
                .replaceAll("eyJ***.***.***");
        }

        @Override
        public boolean containsSensitiveData(String input) {
            return JWT_PATTERN.matcher(input).find();
        }

        @Override
        public MaskerType getType() {
            return MaskerType.JWT_TOKEN;
        }
    }

    /**
     * Маскировщик номеров кредитных карт
     */
    private static class CreditCardMasker implements DataMasker {
        private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
            "\\b\\d{4}[ -]?\\d{4}[ -]?\\d{4}[ -]?\\d{4}\\b");

        @Override
        public String mask(String input) {
            return CREDIT_CARD_PATTERN.matcher(input)
                .replaceAll("**** **** **** $1".substring("$1".length() - 4));
        }

        @Override
        public boolean containsSensitiveData(String input) {
            return CREDIT_CARD_PATTERN.matcher(input).find();
        }

        @Override
        public MaskerType getType() {
            return MaskerType.CREDIT_CARD;
        }
    }
}