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
    SZV_M("src/test/resources/application/szv-m/PFR_154_SZV-M.xml"),
    SZV_TD_TYPE1("src/test/resources/application/szv-td/PFR_154_SZV-TD.xml"),
    SZV_TD_TYPE5("C:\\test\\TD\\TD5.xml"),
    EFS_TD_TYPE2("EFS_TD_UPDATED.xml"),
    SZV_STAZH_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_STAZH_AF2("src/test/resources/application/Szv_Stazh/PFR_154_SZV-STAJ.xml"),
    SZV_KORR_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_KORR_AF2("src/test/resources/application/Szv_Corr/PFR_154_SZV-KORR.xml"),
    SZV_K_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_K_AF2("src/test/resources/application/szv_k/SFR_154_SZV-K.xml"),
    SZV_ISH_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_ISH_AF2("src/test/resources/application/Szv_Ish/PFR_154_SZV-ISH.xml"),
    SZV_DSO_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_DSO_AF2("src/test/resources/application/Szv_Dso/PFR_154_SZV-DSO.xml"),
    SZV_ODV_1_ECP("C:\\test\\TD\\STAZH.xml"),
    SZV_ODV_1_AF2("src/test/resources/application/odv_1/PFR_154_ODV-1.xml"),
    EFS_TD_ECP("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_TD_AF2("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_STAZH_ECP("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_STAZH_AF2("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_OSS_ECP("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_OSS_AF2("C:\\test\\EFS\\EFS_STAZH.xml"),
    EFS_FULL_ECP("src/test/resources/application/EFS/PFR_154_EFS-1.xml"),
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
