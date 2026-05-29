package com.example.personalwebsite.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsOverviewDTO {
    private long totalPosts;
    private long totalComments;
    private long totalViews;
    private long totalUsers;
    private long todayPV;
    private long todayUV;
    private long yesterdayPV;
    private long yesterdayUV;
    private List<RecentPostDTO> recentPosts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentPostDTO {
        private Long id;
        private String title;
        private Long viewCount;
    }
}
