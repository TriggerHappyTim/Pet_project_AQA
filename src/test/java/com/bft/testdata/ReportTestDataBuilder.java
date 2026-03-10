package com.bft.testdata;

import com.bft.enums.ReportFormType;

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
 *     .withReportType(ReportFormType.EFS1)
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
 * @see ReportFormType для типов отчетов в UI
 * @since 2.0
 */
public class ReportTestDataBuilder extends TestDataBuilder<ReportTestDataBuilder.ReportData, ReportTestDataBuilder> {
    
    private ReportFormType reportFormType;
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
            .withReportType(ReportFormType.EFS1)
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
            .withReportType(ReportFormType.values()[faker.number().numberBetween(0, ReportFormType.values().length)])
            .withReportNumber("REPORT-" + faker.number().numberBetween(1000, 9999))
            .withPeriod(startDate, endDate)
            .withStatus(faker.options().option("Черновик", "Отправлен", "Обработан", "Ошибка"))
            .withOrganization(faker.company().name())
            .withProcessId(UUID.randomUUID().toString());
    }
    
    /**
     * Устанавливает тип отчета
     * 
     * @param reportFormType тип отчета для UI
     * @return текущий экземпляр builder для цепочки вызовов
     */
    public ReportTestDataBuilder withReportType(ReportFormType reportFormType) {
        this.reportFormType = reportFormType;
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
     * @throws IllegalArgumentException если данные не прошли валидацию
     */
    @Override
    public ReportData build() {
        // Валидация данных перед созданием объекта
        validateReportData();
        
        return new ReportData(reportFormType, reportNumber, periodStart, periodEnd,
            status, organization, processId, xmlContent);
    }
    
    /**
     * Валидирует данные отчета перед созданием объекта
     * 
     * @throws IllegalArgumentException если данные не прошли валидацию
     */
    private void validateReportData() {
        // Валидация типа отчета
        if (reportFormType == null) {
            throw new IllegalArgumentException("Тип отчета является обязательным полем");
        }
        
        // Валидация номера отчета
        if (reportNumber == null || reportNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Номер отчета является обязательным полем");
        }
        
        // Валидация периода отчета
        if (periodStart != null && periodEnd != null) {
            if (periodStart.isAfter(periodEnd)) {
                throw new IllegalArgumentException(
                    String.format("Начальная дата периода (%s) не может быть позже конечной даты (%s)",
                        periodStart, periodEnd));
            }
            
            // Проверка разумности периода (не более 5 лет назад и не в будущем)
            LocalDate minDate = LocalDate.now().minusYears(5);
            LocalDate maxDate = LocalDate.now().plusDays(1);
            
            if (periodStart.isBefore(minDate)) {
                throw new IllegalArgumentException(
                    String.format("Начальная дата периода (%s) не может быть раньше %s",
                        periodStart, minDate));
            }
            
            if (periodEnd.isAfter(maxDate)) {
                throw new IllegalArgumentException(
                    String.format("Конечная дата периода (%s) не может быть в будущем",
                        periodEnd));
            }
        }
        
        // Валидация статуса
        if (status != null && !status.isEmpty()) {
            String[] validStatuses = {"Черновик", "Отправлен", "Обработан", "Ошибка", "Отклонен"};
            boolean isValidStatus = false;
            for (String validStatus : validStatuses) {
                if (status.equals(validStatus)) {
                    isValidStatus = true;
                    break;
                }
            }
            if (!isValidStatus) {
                throw new IllegalArgumentException(
                    String.format("Некорректный статус отчета: %s. Допустимые значения: %s",
                        status, String.join(", ", validStatuses)));
            }
        }
        
        // Валидация организации
        if (organization != null && organization.trim().isEmpty()) {
            throw new IllegalArgumentException("Название организации не может быть пустым");
        }
        
        // Валидация processId (UUID формата)
        if (processId != null && !processId.isEmpty()) {
            if (!isValidUUID(processId)) {
                throw new IllegalArgumentException("Некорректный формат UUID для processId: " + processId);
            }
        }
        
        // Валидация XML содержимого (если указано)
        if (xmlContent != null && !xmlContent.isEmpty()) {
            if (!isValidXml(xmlContent)) {
                throw new IllegalArgumentException("Некорректный формат XML содержимого");
            }
        }
    }
    
    /**
     * Проверяет валидность UUID
     * 
     * @param uuid строка для проверки
     * @return true если строка является валидным UUID, false в противном случае
     */
    private boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Проверяет базовую валидность XML
     * 
     * @param xml XML строка для проверки
     * @return true если XML имеет базовую валидность, false в противном случае
     */
    private boolean isValidXml(String xml) {
        if (xml == null || xml.trim().isEmpty()) {
            return false;
        }
        // Базовая проверка: XML должен начинаться с < и содержать закрывающие теги
        String trimmed = xml.trim();
        return trimmed.startsWith("<") && 
               (trimmed.contains("</") || trimmed.endsWith("/>") || trimmed.endsWith("?>"));
    }
    
    /**
     * Класс данных для хранения информации об отчете
     */
    public static class ReportData {
        private final ReportFormType reportFormType;
        private final String reportNumber;
        private final LocalDate periodStart;
        private final LocalDate periodEnd;
        private final String status;
        private final String organization;
        private final String processId;
        private final String xmlContent;
        
        public ReportData(ReportFormType reportFormType, String reportNumber, LocalDate periodStart,
                         LocalDate periodEnd, String status, String organization,
                         String processId, String xmlContent) {
            this.reportFormType = reportFormType;
            this.reportNumber = reportNumber;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
            this.status = status;
            this.organization = organization;
            this.processId = processId;
            this.xmlContent = xmlContent;
        }
        
        public ReportFormType getReportFormType() { return reportFormType; }
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
            return String.format("ReportData{reportFormType=%s, reportNumber='%s', status='%s', organization='%s'}",
                reportFormType, reportNumber, status, organization);
        }
    }
}
