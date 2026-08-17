package com.bft.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Утилита для выбора контура (UIType) из переменной окружения или system property.
 * <p>
 * Поддерживает три системы: EVS, РПУ, УОС. Каждая имеет свой ключ:
 * <ul>
 *   <li>{@code evs.ui.type} — контур EVS (ЛК Страхователя / Архива)</li>
 *   <li>{@code rpu.ui.type} — контур РПУ (если не задан, определяется из EVS)</li>
 *   <li>{@code uos.ui.type} — контур УОС (если не задан, определяется из EVS)</li>
 * </ul>
 */
public final class UITypeSelector {

    private static final Logger log = LoggerFactory.getLogger(UITypeSelector.class);

    public static final String EVS_UI_TYPE_PROPERTY = "evs.ui.type";
    public static final String RPU_UI_TYPE_PROPERTY = "rpu.ui.type";
    public static final String UOS_UI_TYPE_PROPERTY = "uos.ui.type";

    public static final UIType DEFAULT_UI_TYPE = UIType.EVS_UAT_LKS;

    private UITypeSelector() {
    }

    /**
     * Возвращает выбранный EVS-контур.
     * Приоритет: System.getProperty → System.getenv → DEFAULT_UI_TYPE.
     */
    public static UIType getSelectedUIType() {
        return resolveType(EVS_UI_TYPE_PROPERTY, DEFAULT_UI_TYPE);
    }

    /**
     * Возвращает выбранный EVS-контур с указанным значением по умолчанию.
     */
    public static UIType getSelectedUIType(UIType defaultType) {
        return resolveType(EVS_UI_TYPE_PROPERTY, defaultType != null ? defaultType : DEFAULT_UI_TYPE);
    }

    /**
     * Возвращает контур РПУ.
     * Приоритет: Явное свойство → Авто-маппинг из EVS (TEST→RPU_TEST, иначе→RPU_UAT).
     */
    public static UIType getSelectedRpuType() {
        UIType explicit = resolveTypeOrNull(RPU_UI_TYPE_PROPERTY);
        if (explicit != null) {
            log.info("Контур РПУ задан явно: {}", explicit);
            return explicit;
        }
        UIType auto = isTestEnvironment() ? UIType.RPU_TEST : UIType.RPU_UAT;
        log.info("Контур РПУ определен автоматически из EVS: {}", auto);
        return auto;
    }

    /**
     * Возвращает контур УОС.
     * Приоритет: Явное свойство → Авто-маппинг из EVS (TEST→UOS_TEST, иначе→UOS_UAT).
     */
    public static UIType getSelectedUosType() {
        UIType explicit = resolveTypeOrNull(UOS_UI_TYPE_PROPERTY);
        if (explicit != null) {
            log.info("Контур УОС задан явно: {}", explicit);
            return explicit;
        }
        UIType auto = isTestEnvironment() ? UIType.UOS_TEST : UIType.UOS_UAT;
        log.info("Контур УОС определен автоматически из EVS: {}", auto);
        return auto;
    }

    /**
     * Определяет, является ли текущий EVS-контур тестовым окружением.
     */
    public static boolean isTestEnvironment() {
        return getSelectedUIType().name().contains("TEST");
    }

    private static UIType resolveType(String propertyKey, UIType defaultType) {
        String value = System.getProperty(propertyKey);
        String source = "System Property";

        if (value == null || value.isBlank()) {
            value = System.getenv(propertyKey);
            source = "Environment Variable";
        }

        if (value == null || value.isBlank()) {
            log.info("{} не задан. Используется значение по умолчанию: {}", propertyKey, defaultType);
            return defaultType;
        }

        UIType result = parseUIType(propertyKey, value.trim());
        log.info("{} установлен в {}. Выбран контур: {}", propertyKey, source, result);
        return result;
    }

    private static UIType resolveTypeOrNull(String propertyKey) {
        String value = System.getProperty(propertyKey);
        if (value == null || value.isBlank()) {
            value = System.getenv(propertyKey);
        }
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseUIType(propertyKey, value.trim());
    }

    private static UIType parseUIType(String propertyKey, String name) {
        try {
            return UIType.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("Неизвестный контур в '%s': '%s'. Доступные: %s",
                            propertyKey, name, java.util.Arrays.toString(UIType.values())),
                    e
            );
        }
    }
}