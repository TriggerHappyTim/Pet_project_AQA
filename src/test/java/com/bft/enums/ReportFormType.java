package com.bft.enums;

/**
 * Тип отчёта для выбора в UI (название формы в интерфейсе).
 *
 * <p>Используется в сценариях выбора типа отчёта (например {@link com.bft.steps.SzvReportsSteps#selectReportType}).
 * Пути к XML-файлам задаются отдельно — см. {@link ReportXmlResource}.
 *
 * @see ReportXmlResource
 */
public enum ReportFormType {
    SZVM("СЗВ-М"),
    SZVTD("СЗВ-ТД"),
    SZVSTAZH("СЗВ-СТАЖ"),
    SZVK("СЗВ-К"),
    SZVKORR("СЗВ-КОРР"),
    SZVISH("СЗВ-ИСХ"),
    SZVDSO("СЗВ-ДСО"),
    ODV1("ОДВ-1"),
    EFS1("ЕФС-1");

    private final String displayName;

    ReportFormType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Название формы для отображения в UI (текст в списке типов отчётов).
     *
     * @return строка для выбора в интерфейсе
     */
    public String getDisplayName() {
        return displayName;
    }
}
