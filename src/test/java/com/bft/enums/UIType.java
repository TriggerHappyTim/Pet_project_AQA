package com.bft.enums;

public enum UIType {
    EVS_TEST("https://front-evs-service.test.ecp/#/"),
    EVS_UAT("https://front-evs-service.uat.ecp/#/"),
    EVS_TEST_LKS("https://evs-oi-portal-front.test.ecp/insurer/"),
    EVS_UAT_LKS("https://ecp-test.sfr.gov.ru/insurer/#/"),
    RPU("https://rpu-common-bo.test.ecp/"),
    RPU_TEST("https://portal.test.ecp/rpu/#/?location=rpu"),
    RPU_UAT("https://rpu-common-bo.uat.ecp/#/"),
    UOS_TEST("https://front-uos-service.test.ecp/"),
    UOS_UAT("https://front-uos-service.uat.ecp/");

    public final String value;

    private UIType(String value) {
        this.value = value;
    }
}
