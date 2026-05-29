package com.example.personalwebsite.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSourceDTO {
    private String source;
    private Long count;
    private Double percentage;
}
