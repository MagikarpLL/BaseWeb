package com.example.personalwebsite.controller.analytics;

import com.example.personalwebsite.model.dto.response.ApiResponse;
import com.example.personalwebsite.model.dto.response.AnalyticsOverviewDTO;
import com.example.personalwebsite.model.dto.response.AnalyticsPageDTO;
import com.example.personalwebsite.model.dto.response.AnalyticsSourceDTO;
import com.example.personalwebsite.model.dto.response.AnalyticsTrendDTO;
import com.example.personalwebsite.model.entity.DailyStats;
import com.example.personalwebsite.model.entity.PageView;
import com.example.personalwebsite.model.entity.UniqueVisitor;
import com.example.personalwebsite.service.BlogPostService;
import com.example.personalwebsite.service.CommentService;
import com.example.personalwebsite.service.DailyStatsService;
import com.example.personalwebsite.service.PageViewService;
import com.example.personalwebsite.service.UniqueVisitorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final PageViewService pageViewService;
    private final UniqueVisitorService uniqueVisitorService;
    private final DailyStatsService dailyStatsService;
    private final BlogPostService blogPostService;
    private final CommentService commentService;

    public AnalyticsController(PageViewService pageViewService,
                               UniqueVisitorService uniqueVisitorService,
                               DailyStatsService dailyStatsService,
                               BlogPostService blogPostService,
                               CommentService commentService) {
        this.pageViewService = pageViewService;
        this.uniqueVisitorService = uniqueVisitorService;
        this.dailyStatsService = dailyStatsService;
        this.blogPostService = blogPostService;
        this.commentService = commentService;
    }

    @GetMapping("/pageviews")
    public ApiResponse<List<PageView>> getPageViews(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PageView> pageViews = pageViewService.findByDateRange(startDate, endDate);
        return ApiResponse.success(pageViews);
    }

    @GetMapping("/visitors")
    public ApiResponse<List<UniqueVisitor>> getVisitors() {
        List<UniqueVisitor> visitors = uniqueVisitorService.findAll();
        return ApiResponse.success(visitors);
    }

    @GetMapping("/visitors/count")
    public ApiResponse<Long> getVisitorCount() {
        long count = uniqueVisitorService.countTotal();
        return ApiResponse.success(count);
    }

    @GetMapping("/daily")
    public ApiResponse<List<DailyStats>> getDailyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailyStats> stats = dailyStatsService.findByDateRange(startDate, endDate);
        return ApiResponse.success(stats);
    }

    @GetMapping("/daily/recent")
    public ApiResponse<List<DailyStats>> getRecentDailyStats(@RequestParam(defaultValue = "30") Integer limit) {
        List<DailyStats> stats = dailyStatsService.findRecent(limit);
        return ApiResponse.success(stats);
    }

    // Admin Analytics Endpoints
    @GetMapping("/admin/analytics/overview")
    public ApiResponse<AnalyticsOverviewDTO> getOverview() {
        AnalyticsOverviewDTO dto = new AnalyticsOverviewDTO();
        dto.setTotalPosts(blogPostService.countPublished());
        dto.setTotalComments(commentService.countAll());
        dto.setTotalViews(blogPostService.sumReadCount());
        dto.setTotalUsers(1L); // Simplified - could track registered users
        
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        Integer todayPv = pageViewService.sumPvByDate(today);
        Integer todayUv = pageViewService.sumUvByDate(today);
        Integer yesterdayPv = pageViewService.sumPvByDate(yesterday);
        Integer yesterdayUv = pageViewService.sumUvByDate(yesterday);
        
        dto.setTodayPV(todayPv != null ? todayPv.longValue() : 0L);
        dto.setTodayUV(todayUv != null ? todayUv.longValue() : 0L);
        dto.setYesterdayPV(yesterdayPv != null ? yesterdayPv.longValue() : 0L);
        dto.setYesterdayUV(yesterdayUv != null ? yesterdayUv.longValue() : 0L);
        
        // Recent posts with view counts
        List<AnalyticsOverviewDTO.RecentPostDTO> recentPostsList = new ArrayList<>();
        List<com.example.personalwebsite.model.entity.BlogPost> posts = blogPostService.findLatestPosts(5);
        for (com.example.personalwebsite.model.entity.BlogPost post : posts) {
            AnalyticsOverviewDTO.RecentPostDTO recentPost = new AnalyticsOverviewDTO.RecentPostDTO();
            recentPost.setId(post.getId());
            recentPost.setTitle(post.getTitle());
            recentPost.setViewCount(post.getReadCount() != null ? post.getReadCount().longValue() : 0L);
            recentPostsList.add(recentPost);
        }
        dto.setRecentPosts(recentPostsList);
        
        return ApiResponse.success(dto);
    }

    @GetMapping("/admin/analytics/trend")
    public ApiResponse<AnalyticsTrendDTO> getTrend(@RequestParam(defaultValue = "7") Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        
        List<DailyStats> stats = dailyStatsService.findByDateRange(startDate, endDate);
        
        // Fill in missing dates with zero values
        Map<LocalDate, DailyStats> statsMap = stats.stream()
                .collect(Collectors.toMap(DailyStats::getDate, s -> s));
        
        List<String> dates = new ArrayList<>();
        List<Integer> pv = new ArrayList<>();
        List<Integer> uv = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dates.add(date.toString());
            DailyStats stat = statsMap.get(date);
            pv.add(stat != null && stat.getTotalPv() != null ? stat.getTotalPv().longValue() : 0L);
            uv.add(stat != null && stat.getTotalUv() != null ? stat.getTotalUv().longValue() : 0L);
        }
        
        AnalyticsTrendDTO dto = new AnalyticsTrendDTO();
        dto.setDates(dates);
        dto.setPv(pv);
        dto.setUv(uv);
        
        return ApiResponse.success(dto);
    }

    @GetMapping("/admin/analytics/pages")
    public ApiResponse<List<AnalyticsPageDTO>> getTopPages(@RequestParam(defaultValue = "10") Integer limit) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        
        List<PageView> topPages = pageViewService.findTopPages(startDate, endDate, limit);
        
        List<AnalyticsPageDTO> result = new ArrayList<>();
        for (PageView pageView : topPages) {
            AnalyticsPageDTO dto = new AnalyticsPageDTO();
            dto.setPath(pageView.getPageUrl());
            dto.setTitle(extractTitleFromUrl(pageView.getPageUrl()));
            dto.setViewCount(pageView.getPvCount() != null ? pageView.getPvCount().longValue() : 0L);
            dto.setUniqueViewCount(pageView.getUvCount() != null ? pageView.getUvCount().longValue() : 0L);
            result.add(dto);
        }
        
        return ApiResponse.success(result);
    }

    @GetMapping("/admin/analytics/sources")
    public ApiResponse<List<AnalyticsSourceDTO>> getSources() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        
        List<PageView> topSources = pageViewService.findTopSources(startDate, endDate, 10);
        
        // Calculate total for percentages
        long total = topSources.stream()
                .mapToLong(s -> s.getPvCount() != null ? s.getPvCount() : 0)
                .sum();
        
        List<AnalyticsSourceDTO> result = new ArrayList<>();
        for (PageView source : topSources) {
            AnalyticsSourceDTO dto = new AnalyticsSourceDTO();
            dto.setSource(normalizeSource(source.getReferrer()));
            dto.setCount(source.getPvCount() != null ? source.getPvCount().longValue() : 0L);
            dto.setPercentage(total > 0 ? (double)(source.getPvCount() * 100) / total : 0.0);
            result.add(dto);
        }
        
        return ApiResponse.success(result);
    }

    private String extractTitleFromUrl(String url) {
        if (url == null || url.isEmpty()) return "Unknown";
        // Extract meaningful title from URL path
        if (url.contains("/blog/")) {
            return "Blog Post"; // In production, would look up the actual post title
        } else if (url.equals("/") || url.equals("")) {
            return "Home";
        } else if (url.startsWith("/tools")) {
            return "Tools";
        } else if (url.equals("/about")) {
            return "About";
        }
        return "Page";
    }

    private String normalizeSource(String referrer) {
        if (referrer == null || referrer.isEmpty() || referrer.equals("direct")) {
            return "Direct";
        }
        try {
            java.net.URL url = new java.net.URL(referrer);
            String host = url.getHost();
            if (host.contains("google")) return "Google";
            if (host.contains("bing")) return "Bing";
            if (host.contains("baidu")) return "Baidu";
            if (host.contains("duckduckgo")) return "DuckDuckGo";
            return "Other";
        } catch (Exception e) {
            return "Other";
        }
    }
}