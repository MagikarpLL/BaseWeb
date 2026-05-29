package com.example.personalwebsite.controller;

import com.example.personalwebsite.model.dto.request.TrackRequest;
import com.example.personalwebsite.model.dto.response.*;
import com.example.personalwebsite.model.entity.BlogPost;
import com.example.personalwebsite.model.entity.Comment;
import com.example.personalwebsite.model.entity.DailyStats;
import com.example.personalwebsite.model.entity.PageView;
import com.example.personalwebsite.model.entity.UniqueVisitor;
import com.example.personalwebsite.model.entity.User;
import com.example.personalwebsite.service.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
public class AdminAnalyticsController {

    private final PageViewService pageViewService;
    private final UniqueVisitorService uniqueVisitorService;
    private final DailyStatsService dailyStatsService;
    private final BlogPostService blogPostService;
    private final CommentService commentService;
    private final UserService userService;

    private static final String VISITOR_COOKIE_NAME = "visitor_id";

    /**
     * Track a page view - public endpoint, no auth required
     * POST /api/analytics/track
     */
    @PostMapping("/analytics/track")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trackPageView(@RequestBody TrackRequest request, HttpServletRequest httpRequest) {
        if (request.getPageUrl() == null || request.getPageUrl().trim().isEmpty()) {
            log.warn("Track request received with empty pageUrl");
            return;
        }

        String visitorId = resolveVisitorId(request.getVisitorId(), httpRequest);
        LocalDate today = LocalDate.now();
        int currentHour = LocalDateTime.now().getHour();

        // Process tracking asynchronously in background
        processTrackingAsync(request, visitorId, today, currentHour);
    }

    private void processTrackingAsync(TrackRequest request, String visitorId, LocalDate today, int currentHour) {
        try {
            // Check if new visitor
            UniqueVisitor visitor = uniqueVisitorService.findByVisitorId(visitorId);
            boolean isNewVisitor = visitor == null;

            if (isNewVisitor) {
                visitor = new UniqueVisitor();
                visitor.setVisitorId(visitorId);
                visitor.setFirstVisitAt(LocalDateTime.now());
                visitor.setLastVisitAt(LocalDateTime.now());
                visitor.setTotalPv(1);
                uniqueVisitorService.createOrUpdate(visitor);
                log.info("New visitor tracked: {}", visitorId);
            } else {
                visitor.setLastVisitAt(LocalDateTime.now());
                visitor.setTotalPv(visitor.getTotalPv() + 1);
                uniqueVisitorService.createOrUpdate(visitor);
            }

            // Record page view
            PageView pageView = new PageView();
            pageView.setPageUrl(request.getPageUrl());
            pageView.setReferrer(request.getReferrer());
            pageView.setDate(today);
            pageView.setHour(currentHour);
            pageView.setPvCount(1);
            pageView.setUvCount(isNewVisitor ? 1 : 0);
            pageViewService.createOrUpdate(pageView);

            // Update daily stats
            updateDailyStats(today);

            log.debug("Tracked page view: {} from visitor: {}", request.getPageUrl(), visitorId);
        } catch (Exception e) {
            log.error("Error tracking page view: {}", e.getMessage(), e);
        }
    }

    private void updateDailyStats(LocalDate date) {
        Integer pv = pageViewService.sumPvByDate(date);
        Integer uv = pageViewService.sumUvByDate(date);

        DailyStats stats = new DailyStats();
        stats.setDate(date);
        stats.setTotalPv(pv != null ? pv : 0);
        stats.setTotalUv(uv != null ? uv : 0);
        dailyStatsService.upsert(stats);
    }

    private String resolveVisitorId(String visitorIdFromRequest, HttpServletRequest request) {
        // Priority: request body > cookie > generate new
        if (visitorIdFromRequest != null && !visitorIdFromRequest.trim().isEmpty()) {
            return visitorIdFromRequest;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (VISITOR_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return UUID.randomUUID().toString();
    }

    /**
     * Get analytics overview - admin endpoint
     * GET /api/admin/analytics/overview
     */
    @GetMapping("/analytics/overview")
    public ApiResponse<AnalyticsOverviewDTO> getOverview() {
        LocalDate today = LocalDate.now();

        // Count posts
        long totalPosts = blogPostService.countPublished();

        // Count comments
        long totalComments = commentService.countAll();

        // Total views (sum of read_count from all published posts)
        long totalViews = blogPostService.sumReadCount();

        // Count users
        long totalUsers = userService.count();

        // Today's PV/UV
        long todayPV = pageViewService.countByDate(today);
        long todayUV = uniqueVisitorService.countByDate(today);

        // Yesterday's PV/UV
        LocalDate yesterday = today.minusDays(1);
        long yesterdayPV = pageViewService.countByDate(yesterday);
        long yesterdayUV = uniqueVisitorService.countByDate(yesterday);

        // Recent posts with view count
        List<BlogPost> recentPosts = blogPostService.findLatestPosts(5);
        List<AnalyticsOverviewDTO.RecentPostDTO> recentPostsData = recentPosts.stream()
                .map(post -> new AnalyticsOverviewDTO.RecentPostDTO(
                        post.getId(),
                        post.getTitle(),
                        post.getReadCount() != null ? post.getReadCount().longValue() : 0L
                ))
                .collect(Collectors.toList());

        AnalyticsOverviewDTO response = new AnalyticsOverviewDTO(
                totalPosts,
                totalComments,
                totalViews,
                totalUsers,
                todayPV,
                todayUV,
                yesterdayPV,
                yesterdayUV,
                recentPostsData
        );

        return ApiResponse.success(response);
    }

    /**
     * Get PV/UV trend for last N days - admin endpoint
     * GET /api/admin/analytics/trend?days=30
     */
    @GetMapping("/analytics/trend")
    public ApiResponse<AnalyticsTrendDTO> getTrend(@RequestParam(defaultValue = "30") Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // Get daily stats for the date range
        List<String> dates = new ArrayList<>();
        List<Long> pvList = new ArrayList<>();
        List<Long> uvList = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current.toString());
            long pv = pageViewService.countByDate(current);
            long uv = uniqueVisitorService.countByDate(current);
            pvList.add(pv);
            uvList.add(uv);
            current = current.plusDays(1);
        }

        AnalyticsTrendDTO response = new AnalyticsTrendDTO(dates, pvList, uvList);
        return ApiResponse.success(response);
    }

    /**
     * Get top pages by PV - admin endpoint
     * GET /api/admin/analytics/pages?limit=10
     */
    @GetMapping("/analytics/pages")
    public ApiResponse<List<AnalyticsPageDTO>> getTopPages(@RequestParam(defaultValue = "10") Integer limit) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30); // Last 30 days

        List<PageView> topPages = pageViewService.findTopPages(startDate, endDate, limit);

        List<AnalyticsPageDTO> response = topPages.stream()
                .map(pv -> new AnalyticsPageDTO(
                        pv.getPageUrl(),
                        pv.getPageUrl(),
                        pv.getPvCount() != null ? pv.getPvCount().longValue() : 0L,
                        pv.getUvCount() != null ? pv.getUvCount().longValue() : 0L
                ))
                .collect(Collectors.toList());

        return ApiResponse.success(response);
    }

    /**
     * Get traffic sources distribution - admin endpoint
     * GET /api/admin/analytics/sources
     */
    @GetMapping("/analytics/sources")
    public ApiResponse<List<AnalyticsSourceDTO>> getSources() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30); // Last 30 days

        List<PageView> topSources = pageViewService.findTopSources(startDate, endDate, 10);

        // Calculate total for percentage
        long totalCount = topSources.stream()
                .mapToLong(pv -> pv.getPvCount() != null ? pv.getPvCount() : 0)
                .sum();

        List<AnalyticsSourceDTO> response = topSources.stream()
                .map(pv -> {
                    String source = pv.getReferrer() != null ? extractDomain(pv.getReferrer()) : "direct";
                    long count = pv.getPvCount() != null ? pv.getPvCount() : 0;
                    double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0;
                    return new AnalyticsSourceDTO(source, count, percentage);
                })
                .collect(Collectors.toList());

        return ApiResponse.success(response);
    }

    private String extractDomain(String url) {
        if (url == null || url.isEmpty()) {
            return "direct";
        }
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host != null) {
                return host.startsWith("www.") ? host.substring(4) : host;
            }
        } catch (Exception e) {
            log.debug("Failed to extract domain from URL: {}", url);
        }
        return "direct";
    }
}
