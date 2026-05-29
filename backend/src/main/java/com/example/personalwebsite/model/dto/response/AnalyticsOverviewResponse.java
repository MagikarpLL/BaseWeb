package com.example.personalwebsite.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsOverviewResponse {
    private Integer todayPv;
    private Integer todayUv;
    private Integer yesterdayPv;
    private Integer yesterdayUv;
    private Long totalPv;
    private Long totalUv;
    private Long onlineCount;
}
