package com.example.personalwebsite.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsPageDTO {
    private String path;
    private String title;
    private Long viewCount;
    private Long uniqueViewCount;
}
