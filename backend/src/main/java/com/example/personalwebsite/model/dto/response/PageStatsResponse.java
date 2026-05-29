package com.example.personalwebsite.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageStatsResponse {
    private String pageUrl;
    private Long pvCount;
}
