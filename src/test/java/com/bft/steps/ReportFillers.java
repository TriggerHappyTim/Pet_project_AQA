package com.bft.steps;

import com.bft.enums.ReportFormType;
import com.bft.testdata.TestDataConstants;

import java.util.EnumMap;
import java.util.Map;

/**
 * Реестр стратегий ручного заполнения отчётов по типу формы.
 *
 * <p><b>Расширение:</b> чтобы добавить новый отчёт, достаточно дописать запись в {@link #REGISTRY}
 * (или, если используется единый каталог {@code ReportCatalog}, добавить enum-значение с нужным
 * {@link ManualReportFiller}). Базовый тест {@code AbstractReportUploadSignTest} подхватит его
 * автоматически — отдельный тест-класс писать не обязательно (хотя можно для кастомных @AllureId).
 *
 * <p>Последовательности шагов для СЗВ-М и СЗВ-ТД выверены по существующим ручным тестам
 * ({@code Szv_m}, {@code Szv_td}). Для остальных типов указаны наиболее вероятные шаги по
 * имеющимся методам {@link SzvReportsSteps}; при необходимости уточнить последовательность на стенде.
 */
public final class ReportFillers {

    private static final Map<ReportFormType, ManualReportFiller> REGISTRY = new EnumMap<>(ReportFormType.class);

    static {
        REGISTRY.put(ReportFormType.SZVM, new SzvMFill());
        REGISTRY.put(ReportFormType.SZVTD, new SzvTdFill());
        REGISTRY.put(ReportFormType.SZVSTAZH, new SzvStazhFill());
        REGISTRY.put(ReportFormType.SZVISH, new SzvIshFill());
        REGISTRY.put(ReportFormType.ODV1, new Odv1Fill());
        REGISTRY.put(ReportFormType.SZVKORR, new SzvKorrFill());
        REGISTRY.put(ReportFormType.SZVK, new SzvKFill());
        REGISTRY.put(ReportFormType.EFS1, new Efs1Fill());
        REGISTRY.put(ReportFormType.SZVDSO, new SzvDsoFill());
        // F4FSS (4-ФСС, code 10) — нет XML-фикстуры и ручных шагов; при появлении добавить сюда.
    }

    private ReportFillers() {
    }

    /** Возвращает стратегию заполнения для типа отчёта (или {@code null}, если не зарегистрирована). */
    public static ManualReportFiller get(ReportFormType type) {
        return REGISTRY.get(type);
    }

    // ---------- Реализации по отчётам ----------

    /** СЗВ-М: общая информация + один застрахованный (минимум) / два ЗЛ (максимум). */
    static final class SzvMFill implements ManualReportFiller {
        @Override
        public void fillMinimal(SzvReportsSteps s) {
            s.addGeneralInfo();
            s.createContinue();
            s.goToZL();
            s.addZL();
            s.fillZL();
            s.fillZLINN();
            s.saveZL();
        }

        @Override
        public void fillMaximal(SzvReportsSteps s) {
            s.addGeneralInfo();
            s.createContinue();
            s.goToZL();
            s.addZL();
            s.fillZL();
            s.fillZLINN();
            s.saveZL();
            // дополнительный застрахованный — максимальная комплектация
            s.goToZL();
            s.addZL();
            s.fillZL();
            s.fillZLINN();
            s.saveZL();
        }

        @Override
        public void verifyPersisted(ReportValidationSteps v) {
            v.verifyTextPresentEventually(TestDataConstants.InsuredPerson.LAST_NAME_1);
        }
    }

    /** СЗВ-ТД: общая информация + ЗЛ + трудовое событие (минимум) / + второе событие (максимум). */
    static final class SzvTdFill implements ManualReportFiller {
        @Override
        public void fillMinimal(SzvReportsSteps s) {
            s.addGeneralInfoTD();
            s.createContinue();
            s.goToZL();
            s.addZL();
            s.fillZL();
            s.fillZLBirth();
            s.addEvent();
            s.saveEvent();
            s.saveZL();
        }

        @Override
        public void fillMaximal(SzvReportsSteps s) {
            s.addGeneralInfoTD();
            s.createContinue();
            s.goToZL();
            s.addZL();
            s.fillZL();
            s.fillZLBirth();
            s.addEvent();
            s.saveEvent();
            // второе трудовое событие
            s.addEvent();
            s.saveEvent();
            s.saveZL();
        }

        @Override
        public void verifyPersisted(ReportValidationSteps v) {
            v.verifyTextPresentEventually(TestDataConstants.InsuredPerson.LAST_NAME_1);
        }
    }

    /** СЗВ-СТАЖ: общая информация + ЗЛ стажа + период стажа (addSTAJ). */
    static final class SzvStazhFill implements ManualReportFiller {
@Override
        public void fillMinimal(SzvReportsSteps s) {
            s.addGeneralInfoSTAGE();
            s.addZL();
            s.fillSectionSTAZH();
            s.saveZL();
        }

        @Override
        public void fillMaximal(SzvReportsSteps s) {
            s.addGeneralInfoSTAGE();
            s.addZL();
            s.fillSectionSTAZH();
            s.saveZL();
            // второй раздел
            s.addZL();
            s.fillSectionSTAZH();
            s.saveZL();
        }

        @Override
        public void verifyPersisted(ReportValidationSteps v) {
            v.verifyTextPresentEventually("Инженер");
        }
    }

    /** СЗВ-ИСХ: общая информация (ISH) + ЗЛ (ИСХ-формат). */
    static final class SzvIshFill implements ManualReportFiller {
        @Override
        public void fillMinimal(SzvReportsSteps s) {
            s.addGeneralInfoISH();
            s.createContinue();
            s.goToZL();
            s.addZL();
            s.fillZLForISH();
            s.saveZL();
        }

        @Override
        public void fillMaximal(SzvReportsSteps s) {
            s.addGeneralInfoISH();
            s.createContinue();
            s.goToZL();
            s.addZL();
            s.fillZLForISH();
            s.saveZL();
            // второе застрахованное лицо
            s.addZL();
            s.fillZLForISH();
            s.saveZL();
        }

        @Override
        public void verifyPersisted(ReportValidationSteps v) {
            v.verifyTextPresentEventually(TestDataConstants.InsuredPerson.LAST_NAME_1);
        }
    }

    /**
     * ЕФС-1: общая информация (EFS) + событие трудовой деятельности + ЗЛ (ЕФС-формат).
     * Примечание: покрыт только «шапка» и раздел 1.1 (ТД). Остальные разделы ЕФС-1
     * (СТАЖ, СЗВ-ПГОС, ОСС, ДСВ-3) требуют новых методов в ReportDataEntrySteps.
     */
    static final class Efs1Fill implements ManualReportFiller {
        @Override
        public void fillMinimal(SzvReportsSteps s) {
            s.addGeneralInfoEFS();
            s.createContinue();
            s.sidebar();
            s.addZL();
            s.fillZLEFS();
            s.addEventEFS();
            s.saveEvent();
            s.saveZLEFS();
        }

        @Override
        public void fillMaximal(SzvReportsSteps s) {
            s.addGeneralInfoEFS();
            s.createContinue();
            s.sidebar();
            s.addZL();
            s.fillZLEFS();
            s.addEventEFS();
            s.saveEvent();
            s.saveZLEFS();
            // второе событие трудовой деятельности (раздел 1.1)
            s.addEventEFS();
            s.saveEvent();
            s.saveZLEFS();
            // раздел СТАЖ (минимальное заполнение, если реализовано)
            s.createEfsStajPerson();
            s.createEfsStajPeriod();
        }

        @Override
        public void verifyPersisted(ReportValidationSteps v) {
            v.verifyTextPresentEventually(TestDataConstants.InsuredPerson.LAST_NAME_1);
        }
    }

    /** ОДВ-1: общая информация + как минимум один период (сведения о подразделении). */
    static final class Odv1Fill implements ManualReportFiller {
        @Override
        public void fillMinimal(SzvReportsSteps s) {
            s.addGeneralInfoODV1();
            s.addZL();
            s.fillOdv1Period();
        }

        @Override
        public void fillMaximal(SzvReportsSteps s) {
            s.addGeneralInfoODV1();
            s.addZL();
            s.fillOdv1Period();
            // корректирующий период (дополнительная запись)
            s.fillOdv1Period();
        }

        @Override
        public void verifyPersisted(ReportValidationSteps v) {
            v.verifyTextPresentEventually("Инженер");
        }
    }

    /** СЗВ-КОРР: содержит СЗВ-М часть (ЗЛ) + сведения корректировки. */
static final class SzvKorrFill implements ManualReportFiller {
        @Override
        public void fillMinimal(SzvReportsSteps s) {
            s.addGeneralInfoKORR();
            s.addZL();
            s.fillSectionSTAZH();
            s.saveZL();
        }

        @Override
        public void fillMaximal(SzvReportsSteps s) {
            s.addGeneralInfoKORR();
            s.addZL();
            s.fillSectionSTAZH();
            s.saveZL();
            // дополнительный раздел
            s.addZL();
            s.fillSectionSTAZH();
            s.saveZL();
        }

        @Override
        public void verifyPersisted(ReportValidationSteps v) {
            v.verifyTextPresentEventually("Инженер");
        }
    }

    /** СЗВ-К: общая информация + ЗЛ (К-формат: personSurname/Name/Middlename + ДР). */
    static final class SzvKFill implements ManualReportFiller {
        @Override
        public void fillMinimal(SzvReportsSteps s) {
            s.addGeneralInfoK();
            s.createContinue();
            s.goToZL();
            s.addZL();
            s.addZL_K();
            s.saveZL();
        }

        @Override
        public void fillMaximal(SzvReportsSteps s) {
            s.addGeneralInfoK();
            s.createContinue();
            s.goToZL();
            s.addZL();
            s.addZL_K();
            s.saveZL();
            // второе застрахованное лицо
            s.goToZL();
            s.addZL();
            s.addZL_K();
            s.saveZL();
        }

        @Override
        public void verifyPersisted(ReportValidationSteps v) {
            v.verifyTextPresentEventually(TestDataConstants.InsuredPerson.LAST_NAME_1);
        }
    }

    /**
     * СЗВ-DSO: общая информация + ЗЛ (DSO-формат) + периоды DSO-L и DSO-U.
     * Примечание: разделы ЕФС-1 (СЗВ-ПГОС, ОСС, ДСВ-3) не реализованы — TODO.
     */
    static final class SzvDsoFill implements ManualReportFiller {
        @Override
        public void fillMinimal(SzvReportsSteps s) {
            s.addGeneralInfoDSO();
            s.createContinue();
            s.goToZL();
            s.addZL();
            s.addZL_DSO();
            s.saveZL();
            s.addPeriodDsol();
            s.addPeriodDsou();
        }

        @Override
        public void fillMaximal(SzvReportsSteps s) {
            s.addGeneralInfoDSO();
            s.createContinue();
            s.goToZL();
            s.addZL();
            s.addZL_DSO();
            s.saveZL();
            // дополнительное застрахованное лицо и периоды
            s.goToZL();
            s.addZL();
            s.addZL_DSO();
            s.saveZL();
            s.addPeriodDsol();
            s.addPeriodDsou();
        }

        @Override
        public void verifyPersisted(ReportValidationSteps v) {
            v.verifyTextPresentEventually(TestDataConstants.InsuredPerson.LAST_NAME_1);
        }
    }
}

