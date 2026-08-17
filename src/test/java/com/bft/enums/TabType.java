package com.bft.enums;

/**
 * Типы строк таблицы для использования в локаторах Selenide.
 * Содержит XPath-выражения для различных состояний строк в таблицах N2O.
 */
public enum TabType {
    /**
     * Активная строка таблицы (с классом table-active).
     */
    TABLE("//tr[@class='n2o-advanced-table-row n2o-advanced-table-row-level-0 n2o-table-row n2o-advanced-table-row table-active']"),

    /**
     * Стандартная строка таблицы (без дополнительных модификаторов).
     */
    TABLEACTIVE("//tr[@class='n2o-advanced-table-row n2o-advanced-table-row-level-0 n2o-table-row n2o-advanced-table-row']"),

    /**
     * Строка таблицы с поддержкой действий (клик, row-click).
     */
    ADVANCEDTABLE("//tr[@class='n2o-advanced-table-row n2o-advanced-table-row-level-0 n2o-table-row n2o-advanced-table-row n2o-advanced-table-row--with-action row-click']");

    public final String value;

    TabType(String value) {
        this.value = value;
    }
}