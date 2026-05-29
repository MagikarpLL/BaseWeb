package com.example.personalwebsite.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SiteSettings {
    private Long id;
    private String settingKey;
    private String value;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}