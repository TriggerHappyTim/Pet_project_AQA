package com.bft.enums;

/**
 * Ресурс XML-файла для загрузки отчёта (путь к файлу).
 *
 * <p>Используется при загрузке отчёта через XML (например {@link com.bft.steps.SzvReportsSteps#sendXml}).
 * Тип отчёта для выбора в UI задаётся отдельно — см. {@link ReportFormType}.
 *
 * <p>Пути могут быть относительными (от корня проекта) или абсолютными;
 * абсолютные пути (например {@code C:\test\...}) требуют наличия файлов на машине запуска.
 *
 * @see ReportFormType
 */
public enum ReportXmlResource {
    SZV_M("src/test/resources/application/szv-m/SZV-M.xml"),
    SZV_TD_TYPE1("src/test/resources/application/szv-td/SZV_TD.xml"),
    SZV_TD_TYPE5("C:\\test\\TD\\TD5.xml"),
    EFS_TD_TYPE2("EFS_TD_UPDATED.xml"),
    SZV_STAZH_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_STAZH_AF2("src/test/resources/application/Szv_Stazh/СЗВ-СТАЖ исх.xml"),
    SZV_KORR_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_KORR_AF2("src/test/resources/application/Szv_Corr/СЗВ_КОРРОСОБ_стажУвол+льготный+выплатыС_ДТ.xml"),
    SZV_K_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_K_AF2("src/test/resources/application/szv_k/СЗВ-К_20260101.xml"),
    SZV_ISH_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_ISH_AF2("src/test/resources/application/Szv_Ish/СЗВ-ИСХ_Сведения о стаже.xml"),
    SZV_DSO_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_DSO_AF2("src/test/resources/application/Szv_Dso/1_SZV_DSO_ish_DSOL.xml"),
    SZV_ODV_1_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_ODV_1_AF2("src/test/resources/application/odv_1/СФР_034-356-125690_034044_ОДВ-1_20230403_62b8d0c2-eba9-452f-b1f6-4da928cd11c6.xml"),
    EFS_TD_ECP("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_TD_AF2("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_STAZH_ECP("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_STAZH_AF2("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_OSS_ECP("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_OSS_AF2("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_FULL_ECP("src/test/resources/application/EFS/ЕФС-1_20260101.xml"),
    EFS_FULL_AF2("C:\\test\\EFS\\EFS_STAZH.xml"),
    DOC_PNG("src/test/resources/img/1.png");

    private final String path;

    ReportXmlResource(String path) {
        this.path = path;
    }

    /**
     * Путь к файлу (относительный от корня проекта или абсолютный).
     *
     * @return путь к XML или иному ресурсу
     */
    public String getPath() {
        return path;
    }
}
