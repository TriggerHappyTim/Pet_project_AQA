package com.bft.integration.mapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO для результата выполнения теста
 * 
 * <p>Используется для передачи данных между различными компонентами интеграции.
 */
public class TestResult {
    
    private String testKey;
    private String testName;
    private String testCycle;
    private TestStatus status;
    private Duration duration;
    private LocalDateTime executedAt;
    private String comment;
    private String errorMessage;
    private String stackTrace;
    private List<String> attachments;
    
    public TestResult() {
        this.attachments = new ArrayList<>();
        this.executedAt = LocalDateTime.now();
    }
    
    // Builder pattern
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final TestResult result;
        
        public Builder() {
            this.result = new TestResult();
        }
        
        public Builder testKey(String testKey) {
            result.testKey = testKey;
            return this;
        }
        
        public Builder testName(String testName) {
            result.testName = testName;
            return this;
        }
        
        public Builder testCycle(String testCycle) {
            result.testCycle = testCycle;
            return this;
        }
        
        public Builder status(TestStatus status) {
            result.status = status;
            return this;
        }
        
        public Builder duration(Duration duration) {
            result.duration = duration;
            return this;
        }
        
        public Builder executedAt(LocalDateTime executedAt) {
            result.executedAt = executedAt;
            return this;
        }
        
        public Builder comment(String comment) {
            result.comment = comment;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            result.errorMessage = errorMessage;
            return this;
        }
        
        public Builder stackTrace(String stackTrace) {
            result.stackTrace = stackTrace;
            return this;
        }
        
        public Builder addAttachment(String attachmentPath) {
            result.attachments.add(attachmentPath);
            return this;
        }
        
        public TestResult build() {
            return result;
        }
    }
    
    // Getters and Setters
    
    public String getTestKey() {
        return testKey;
    }
    
    public void setTestKey(String testKey) {
        this.testKey = testKey;
    }
    
    public String getTestName() {
        return testName;
    }
    
    public void setTestName(String testName) {
        this.testName = testName;
    }
    
    public String getTestCycle() {
        return testCycle;
    }
    
    public void setTestCycle(String testCycle) {
        this.testCycle = testCycle;
    }
    
    public TestStatus getStatus() {
        return status;
    }
    
    public void setStatus(TestStatus status) {
        this.status = status;
    }
    
    public Duration getDuration() {
        return duration;
    }
    
    public void setDuration(Duration duration) {
        this.duration = duration;
    }
    
    public LocalDateTime getExecutedAt() {
        return executedAt;
    }
    
    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getStackTrace() {
        return stackTrace;
    }
    
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
    
    public List<String> getAttachments() {
        return attachments;
    }
    
    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }
    
    @Override
    public String toString() {
        return "TestResult{" +
            "testKey='" + testKey + '\'' +
            ", testName='" + testName + '\'' +
            ", status=" + status +
            ", duration=" + duration +
            ", executedAt=" + executedAt +
            '}';
    }
}
