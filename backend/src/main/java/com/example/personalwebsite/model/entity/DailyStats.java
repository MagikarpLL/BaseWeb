package com.example.personalwebsite.model.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DailyStats {
    private Long id;
    private LocalDate date;
    private Integer totalPv;
    private Integer totalUv;
    private LocalDateTime createdAt;
}