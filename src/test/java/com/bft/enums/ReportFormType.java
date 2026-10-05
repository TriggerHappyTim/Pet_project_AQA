package com.bft.enums;

import java.util.Arrays;

/**
 * Тип отчёта для выбора в UI (название формы в интерфейсе).
 *
 * <p>Используется в сценариях выбора типа отчёта.
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
    EFS1("ЕФС-1"),
    F4FSS("4-ФСС");

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

    /**
     * Находит тип отчёта по его отображаемому имени в UI.
     * Регистронезависимое сравнение.
     *
     * @param name имя, отображаемое в интерфейсе
     * @return соответствующий enum или null, если не найдено
     */
    public static ReportFormType fromDisplayName(String name) {
        if (name == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(type -> type.displayName.equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}