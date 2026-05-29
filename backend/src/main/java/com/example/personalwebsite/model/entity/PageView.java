package com.example.personalwebsite.model.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PageView {
    private Long id;
    private String pageUrl;
    private String referrer;
    private Integer pvCount;
    private Integer uvCount;
    private LocalDate date;
    private Integer hour;
    private LocalDateTime createdAt;
}