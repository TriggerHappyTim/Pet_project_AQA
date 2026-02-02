package com.bft.testdata;

import com.bft.enums.ReportType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Builder для создания тестовых данных отчетов
 * 
 * <p>Предоставляет удобный способ создания объектов отчетов для тестов
 * с использованием Builder pattern и Faker для генерации реалистичных данных.
 * 
 * <p><b>Пример использования:</b>
 * <pre>{@code
 * // Создание отчета с дефолтными значениями
 * ReportData defaultReport = ReportTestDataBuilder.createDefault().build();
 * 
 * // Создание отчета ЕФС-1
 * ReportData efs1Report = ReportTestDataBuilder.createDefault()
 *     .withReportType(ReportType.EFS_1)
 *     .withReportNumber("EFS-1-2024-001")
 *     .withPeriod(LocalDate.now().minusMonths(1), LocalDate.now())
 *     .build();
 * 
 * // Создание случайного отчета
 * ReportData randomReport = ReportTestDataBuilder.createRandom().build();
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see ReportType для типов отчетов
 * @since 2.0
 */
public class ReportTestDataBuilder extends TestDataBuilder<ReportTestDataBuilder.ReportData, ReportTestDataBuilder> {
    
    private ReportType reportType;
    private String reportNumber;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String status;
    private String organization;
    private String processId;
    private String xmlContent;
    
    /**
     * Создает новый builder с дефолтными значениями
     * 
     * @return новый экземпляр ReportTestDataBuilder
     */
    public static ReportTestDataBuilder createDefault() {
        LocalDate now = LocalDate.now();
        return new ReportTestDataBuilder()
            .withReportType(ReportType.EFS_1)
            .withReportNumber("REPORT-" + now.getYear() + "-001")
            .withPeriod(now.minusMonths(1), now)
            .withStatus("Черновик")
            .withOrganization("Тестовая организация")
            .withProcessId(UUID.randomUUID().toString());
    }
    
    /**
     * Создает новый builder со случайными значениями
     * 
     * @return новый экземпляр ReportTestDataBuilder со случайными данными
     */
    public static ReportTestDataBuilder createRandom() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(faker.number().numberBetween(1, 12));
        
        return new ReportTestDataBuilder()
            .withReportType(ReportType.values()[faker.number().numberBetween(0, ReportType.values().length)])
            .withReportNumber("REPORT-" + faker.number().numberBetween(1000, 9999))
            .withPeriod(startDate, endDate)
            .withStatus(faker.options().option("Черновик", "Отправлен", "Обработан", "Ошибка"))
            .withOrganization(faker.company().name())
            .withProcessId(UUID.randomUUID().toString());
    }
    
    /**
     * Устанавливает тип отчета
     * 
     * @param reportType тип отчета
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public ReportTestDataBuilder withReportType(ReportType reportType) {
        this.reportType = reportType;
        return self();
    }
    
    /**
     * Устанавливает номер отчета
     * 
     * @param reportNumber номер отчета
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public ReportTestDataBuilder withReportNumber(String reportNumber) {
        this.reportNumber = reportNumber;
        return self();
    }
    
    /**
     * Устанавливает период отчета
     * 
     * @param startDate начальная дата периода
     * @param endDate конечная дата периода
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public ReportTestDataBuilder withPeriod(LocalDate startDate, LocalDate endDate) {
        this.periodStart = startDate;
        this.periodEnd = endDate;
        return self();
    }
    
    /**
     * Устанавливает статус отчета
     * 
     * @param status статус отчета
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public ReportTestDataBuilder withStatus(String status) {
        this.status = status;
        return self();
    }
    
    /**
     * Устанавливает организацию для отчета
     * 
     * @param organization название организации
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public ReportTestDataBuilder withOrganization(String organization) {
        this.organization = organization;
        return self();
    }
    
    /**
     * Устанавливает ID процесса для отчета
     * 
     * @param processId ID процесса (UUID)
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public ReportTestDataBuilder withProcessId(String processId) {
        this.processId = processId;
        return self();
    }
    
    /**
     * Устанавливает XML содержимое отчета
     * 
     * @param xmlContent XML содержимое
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public ReportTestDataBuilder withXmlContent(String xmlContent) {
        this.xmlContent = xmlContent;
        return self();
    }
    
    /**
     * Строит объект ReportData на основе настроек builder
     * 
     * @return объект ReportData с установленными значениями
     */
    @Override
    public ReportData build() {
        return new ReportData(reportType, reportNumber, periodStart, periodEnd,
            status, organization, processId, xmlContent);
    }
    
    /**
     * Класс данных для хранения информации об отчете
     */
    public static class ReportData {
        private final ReportType reportType;
        private final String reportNumber;
        private final LocalDate periodStart;
        private final LocalDate periodEnd;
        private final String status;
        private final String organization;
        private final String processId;
        private final String xmlContent;
        
        public ReportData(ReportType reportType, String reportNumber, LocalDate periodStart,
                         LocalDate periodEnd, String status, String organization,
                         String processId, String xmlContent) {
            this.reportType = reportType;
            this.reportNumber = reportNumber;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
            this.status = status;
            this.organization = organization;
            this.processId = processId;
            this.xmlContent = xmlContent;
        }
        
        public ReportType getReportType() { return reportType; }
        public String getReportNumber() { return reportNumber; }
        public LocalDate getPeriodStart() { return periodStart; }
        public LocalDate getPeriodEnd() { return periodEnd; }
        public String getStatus() { return status; }
        public String getOrganization() { return organization; }
        public String getProcessId() { return processId; }
        public String getXmlContent() { return xmlContent; }
        
        /**
         * Возвращает период в формате строки (DD.MM.YYYY - DD.MM.YYYY)
         * 
         * @return период в формате строки
         */
        public String getPeriodAsString() {
            if (periodStart == null || periodEnd == null) {
                return "";
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            return periodStart.format(formatter) + " - " + periodEnd.format(formatter);
        }
        
        @Override
        public String toString() {
            return String.format("ReportData{reportType=%s, reportNumber='%s', status='%s', organization='%s'}",
                reportType, reportNumber, status, organization);
        }
    }
}
