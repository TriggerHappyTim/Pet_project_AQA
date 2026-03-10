package com.bft.enums;

/**
 * Утилита для выбора контура (UIType) из переменной окружения или system property.
 * <p>
 * Поддерживает три системы: EVS, РПУ, УОС. Каждая имеет свой ключ:
 * <ul>
 *   <li>{@code evs.ui.type} — контур EVS (ЛК Страхователя / Архива)</li>
 *   <li>{@code rpu.ui.type} — контур РПУ (если не задан, определяется из EVS)</li>
 *   <li>{@code uos.ui.type} — контур УОС (если не задан, определяется из EVS)</li>
 * </ul>
 * <p>
 * <b>Локально:</b>
 * <pre>
 * # PowerShell
 * mvn test -Devs.ui.type=EVS_TEST_LKS -Dtest=Efs1#efs_1_xml
 *
 * # РПУ и УОС определятся автоматически из EVS (TEST → RPU_TEST, UOS_TEST)
 * # Или можно задать явно:
 * mvn test -Devs.ui.type=EVS_UAT_LKS -Drpu.ui.type=RPU_TEST -Dtest=ArchivesTest
 * </pre>
 * <p>
 * <b>GitLab CI:</b> в Variables задать <code>evs.ui.type</code> = <code>EVS_UAT_LKS</code> или
 * передать через Maven profile (см. pom.xml profiles).
 *
 * @see UIType
 */
public final class UITypeSelector {

    public static final String EVS_UI_TYPE_PROPERTY = "evs.ui.type";
    public static final String RPU_UI_TYPE_PROPERTY = "rpu.ui.type";
    public static final String UOS_UI_TYPE_PROPERTY = "uos.ui.type";

    public static final UIType DEFAULT_UI_TYPE = UIType.EVS_UAT_LKS;

    private UITypeSelector() {
    }

    /**
     * Возвращает выбранный EVS-контур из system property или переменной окружения.
     * <p>
     * Приоритет: 1) System.getProperty, 2) System.getenv, 3) DEFAULT_UI_TYPE.
     *
     * @return UIType для авторизации в EVS
     */
    public static UIType getSelectedUIType() {
        return resolveType(EVS_UI_TYPE_PROPERTY, DEFAULT_UI_TYPE);
    }

    /**
     * Возвращает выбранный EVS-контур с указанным значением по умолчанию.
     *
     * @param defaultType контур по умолчанию, если переменная не задана
     * @return UIType для авторизации в EVS
     */
    public static UIType getSelectedUIType(UIType defaultType) {
        return resolveType(EVS_UI_TYPE_PROPERTY, defaultType != null ? defaultType : DEFAULT_UI_TYPE);
    }

    /**
     * Возвращает контур РПУ.
     * <p>
     * Приоритет: 1) {@code rpu.ui.type} property/env, 2) автоматический маппинг из EVS-контура.
     * <ul>
     *   <li>EVS содержит "TEST" → {@code RPU_TEST}</li>
     *   <li>EVS содержит "UAT" или иное → {@code RPU_UAT}</li>
     * </ul>
     *
     * @return UIType для авторизации в РПУ
     */
    public static UIType getSelectedRpuType() {
        UIType explicit = resolveTypeOrNull(RPU_UI_TYPE_PROPERTY);
        if (explicit != null) {
            return explicit;
        }
        return isTestEnvironment() ? UIType.RPU_TEST : UIType.RPU_UAT;
    }

    /**
     * Возвращает контур УОС.
     * <p>
     * Приоритет: 1) {@code uos.ui.type} property/env, 2) автоматический маппинг из EVS-контура.
     * <ul>
     *   <li>EVS содержит "TEST" → {@code UOS_TEST}</li>
     *   <li>EVS содержит "UAT" или иное → {@code UOS_UAT}</li>
     * </ul>
     *
     * @return UIType для авторизации в УОС
     */
    public static UIType getSelectedUosType() {
        UIType explicit = resolveTypeOrNull(UOS_UI_TYPE_PROPERTY);
        if (explicit != null) {
            return explicit;
        }
        return isTestEnvironment() ? UIType.UOS_TEST : UIType.UOS_UAT;
    }

    /**
     * Определяет, является ли текущий EVS-контур тестовым окружением.
     *
     * @return {@code true} если имя выбранного EVS-контура содержит "TEST"
     */
    public static boolean isTestEnvironment() {
        return getSelectedUIType().name().contains("TEST");
    }

    private static UIType resolveType(String propertyKey, UIType defaultType) {
        String value = System.getProperty(propertyKey);
        if (value == null || value.isBlank()) {
            value = System.getenv(propertyKey);
        }
        if (value == null || value.isBlank()) {
            return defaultType;
        }
        return parseUIType(propertyKey, value.trim());
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
