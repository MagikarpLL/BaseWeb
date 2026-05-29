package com.example.personalwebsite.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UniqueVisitor {
    private Long id;
    private String visitorId;
    private LocalDateTime firstVisitAt;
    private LocalDateTime lastVisitAt;
    private Integer totalPv;
}