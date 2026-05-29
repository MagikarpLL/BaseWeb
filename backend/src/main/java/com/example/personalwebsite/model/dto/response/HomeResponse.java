package com.example.personalwebsite.model.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class HomeResponse {
    private ProfileInfo profile;
    private List<PostSummary> latestPosts;
    private List<ToolSummary> featuredTools;

    @Data
    public static class ProfileInfo {
        private String name;
        private String title;
        private String bio;
        private String avatar;
        private List<SocialLink> socialLinks;
    }

    @Data
    public static class SocialLink {
        private String platform;
        private String url;
    }

    @Data
    public static class PostSummary {
        private Long id;
        private String title;
        private String slug;
        private String excerpt;
        private String publishedAt;
        private Integer readCount;
        private String categoryName;
    }

    @Data
    public static class ToolSummary {
        private Long id;
        private String name;
        private String icon;
        private String description;
        private String path;
    }
}