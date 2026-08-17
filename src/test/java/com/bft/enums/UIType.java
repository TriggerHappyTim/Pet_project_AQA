package com.bft.enums;

/**
 * Перечисление доступных окружений и типов интерфейсов (UI) для тестирования.
 * Содержит базовые URL для различных сред (Test, UAT) и модулей системы (EVS, RPU, UOS).
 */
public enum UIType {
    /** EVS Test окружение (базовый путь) */
    EVS_TEST("https://front-evs-service.test.ecp/#/"),

    /** EVS UAT окружение (базовый путь) */
    EVS_UAT("https://front-evs-service.uat.ecp/#/"),

    /** EVS Test окружение для Страхователей (ЛКС) */
    EVS_TEST_LKS("https://evs-oi-portal-front.test.ecp/insurer/"),

    /** EVS UAT окружение для Страхователей (ЛКС) - боевой адрес тестирования */
    EVS_UAT_LKS("https://ecp-test.sfr.gov.ru/insurer/#/"),

    /** EVS Test окружение для Архива (ЛКА) */
    EVS_TEST_LKA("https://evs-oi-portal-front.test.ecp/archive/#/"),

    /** EVS UAT окружение для Архива (ЛКА) */
    EVS_UAT_LKA("https://ecp-test.sfr.gov.ru/archive/#/"),

    /** RPU базовое окружение (устаревшее или общее) */
    RPU("https://rpu-common-bo.test.ecp/"),

    /** RPU Test окружение (портал) */
    RPU_TEST("https://portal.test.ecp/rpu/#/?location=rpu"),

    /** RPU UAT окружение */
    RPU_UAT("https://rpu-common-bo.uat.ecp/#/"),

    /** UOS Test окружение */
    UOS_TEST("https://front-uos-service.test.ecp/"),

    /** UOS UAT окружение */
    UOS_UAT("https://front-uos-service.uat.ecp/");

    /** Базовый URL для данного типа окружения */
    public final String value;

    UIType(String value) {
        this.value = value;
    }
}