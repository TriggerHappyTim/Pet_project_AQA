package com.bft.helpers;

import java.util.Random;

public class TestDataGenerator {
    private static final Random random = new Random();

    public static String generateRandomSnils() {
        return String.format("%03d-%03d-%03d %02d",
                random.nextInt(1000),
                random.nextInt(1000),
                random.nextInt(1000),
                random.nextInt(100));
    }

    public static String generateRandomInn() {
        return String.valueOf(1000000000L + random.nextInt(900000000));
    }

    public static String generateRandomOrgName() {
        String[] orgTypes = {"ООО", "АО", "ЗАО", "ИП"};
        String[] adjectives = {"Тестовое", "Производственное", "Торговое", "Строительное"};
        String[] nouns = {"Предприятие", "Объединение", "Хозяйство", "Агентство"};

        return orgTypes[random.nextInt(orgTypes.length)] + " '" +
                adjectives[random.nextInt(adjectives.length)] + " " +
                nouns[random.nextInt(nouns.length)] + "'";
    }
}
