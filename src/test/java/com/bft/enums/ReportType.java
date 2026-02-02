package com.bft.enums;

public enum ReportType {
    SZVM("СЗВ-М"),
    SZVTD("СЗВ-ТД"),
    SZVSTAZH("СЗВ-СТАЖ"),
    SZVK("СЗВ-К"),
    SZVKORR("СЗВ-КОРР"),
    SZVISH("СЗВ-ИСХ"),
    SZVDSO("СЗВ-ДСО"),
    ODV1("ОДВ-1"),
    EFS1("ЕФС-1"),
/*------------------Отчеты-XML-----------------------------*/
    SZV_M("src/test/resources/application/szv-m/SZV-M.xml"),
    SZV_TD_TYPE1("src/test/resources/application/szv-td/SZV_TD.xml"),
    SZV_TD_TYPE5("C:\\test\\TD\\TD5.xml"),
    EFS_TD_TYPE2("EFS_TD_UPDATED.xml"),
    SZV_STAZH_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_STAZH_AF2("src/test/resources/application/Szv_Stazh/СЗВ-СТАЖ исх.xml"),
    SZV_KORR_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_KORR_AF2("src/test/resources/application/Szv_Corr/СЗВ_КОРРОСОБ_стажУвол+льготный+выплатыС_ДТ.xml"),
    SZV_K_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_K_AF2("C:\\test\\TD\\STAZH.xml"),
    SZV_ISH_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_ISH_AF2("src/test/resources/application/Szv_Ish/СЗВ-ИСХ_Сведения о стаже.xml"),
    SZV_DSO_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_DSO_AF2("src/test/resources/application/Szv_Dso/1_SZV_DSO_ish_DSOL.xml"),
    SZV_ODV_1_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_ODV_1_AF2("C:\\test\\TD\\STAZH.xml"),
    EFS_TD_ECP("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_TD_AF2("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_STAZH_ECP("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_STAZH_AF2("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_OSS_ECP("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_OSS_AF2("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_FULL_ECP("src/test/resources/application/EFS/efsBase1.xml"),
    EFS_FULL_AF2("C:\\test\\EFS\\EFS_STAZH.xml"),
/*----------------------------------------------------------*/
    DocPNG("src/test/resources/img/1.png");

    public final String value;

    private ReportType(String value) {
        this.value = value;
    }
}
