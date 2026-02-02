package com.bft.enums;

public enum TabType {
    TABLE("//tr[@class='n2o-advanced-table-row n2o-advanced-table-row-level-0 n2o-table-row n2o-advanced-table-row table-active']"),
    TABLEACTIVE("//tr[@class='n2o-advanced-table-row n2o-advanced-table-row-level-0 n2o-table-row n2o-advanced-table-row']"),
    ADVANCEDTABLE("//tr[@class='n2o-advanced-table-row n2o-advanced-table-row-level-0 n2o-table-row n2o-advanced-table-row n2o-advanced-table-row--with-action row-click']");

    public final String value;

    private TabType(String value) {
        this.value = value;
    }
}
