package com.bft.enums;

/**
 * Утилита для выбора контура (UIType) из переменной окружения или system property.
 * <p>
 * Подходит для локального запуска и GitLab CI: один и тот же тест может идти в UAT или TEST
 * без изменения кода.
 * <p>
 * <b>Локально:</b>
 * <pre>
 * # PowerShell
 * $env:evs.ui.type = "EVS_UAT_LKS"
 * mvn test -Dtest=Efs1#efs_1_xml
 *
 * # Или через Maven
 * mvn test -Devs.ui.type=EVS_TEST_LKS -Dtest=Efs1#efs_1_xml
 * </pre>
 * <p>
 * <b>GitLab CI:</b> в Variables задать <code>evs.ui.type</code> = <code>EVS_UAT_LKS</code> или
 * передать через Maven profile (см. pom.xml profiles).
 *
 * @see UIType
 */
public final class UITypeSelector {

    /**
     * Ключ для выбора контура: system property или переменная окружения.
     */
    public static final String EVS_UI_TYPE_PROPERTY = "evs.ui.type";

    /**
     * Контур по умолчанию, если переменная не задана.
     */
    public static final UIType DEFAULT_UI_TYPE = UIType.EVS_UAT_LKS;

    private UITypeSelector() {
    }

    /**
     * Возвращает выбранный контур из system property или переменной окружения.
     * <p>
     * Приоритет: 1) System.getProperty, 2) System.getenv, 3) DEFAULT_UI_TYPE.
     *
     * @return UIType для авторизации
     */
    public static UIType getSelectedUIType() {
        String value = System.getProperty(EVS_UI_TYPE_PROPERTY);
        if (value == null || value.isBlank()) {
            value = System.getenv(EVS_UI_TYPE_PROPERTY);
        }
        if (value == null || value.isBlank()) {
            return DEFAULT_UI_TYPE;
        }
        String name = value.trim();
        try {
            return UIType.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format("Неизвестный контур в '%s': '%s'. Доступные: EVS_UAT_LKS, EVS_TEST_LKS, EVS_UAT, EVS_TEST, RPU, RPU_TEST, RPU_UAT, UOS_TEST, UOS_UAT",
                    EVS_UI_TYPE_PROPERTY, name),
                e
            );
        }
    }

    /**
     * Возвращает выбранный контур с указанным значением по умолчанию.
     *
     * @param defaultType контур по умолчанию, если переменная не задана
     * @return UIType для авторизации
     */
    public static UIType getSelectedUIType(UIType defaultType) {
        String value = System.getProperty(EVS_UI_TYPE_PROPERTY);
        if (value == null || value.isBlank()) {
            value = System.getenv(EVS_UI_TYPE_PROPERTY);
        }
        if (value == null || value.isBlank()) {
            return defaultType != null ? defaultType : DEFAULT_UI_TYPE;
        }
        String name = value.trim();
        try {
            return UIType.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format("Неизвестный контур в '%s': '%s'", EVS_UI_TYPE_PROPERTY, name),
                e
            );
        }
    }
}
