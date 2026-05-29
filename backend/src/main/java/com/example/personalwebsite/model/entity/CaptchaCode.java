package com.example.personalwebsite.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CaptchaCode {
    private String code;
    private String sessionId;
    private LocalDateTime createdAt;
    private int attempts;
    
    public boolean isExpired() {
        return createdAt.plusMinutes(10).isBefore(LocalDateTime.now());
    }
    
    public boolean isValid(String inputCode) {
        return !isExpired() && code.equalsIgnoreCase(inputCode) && attempts < 5;
    }
    
    public void incrementAttempts() {
        this.attempts++;
    }
}