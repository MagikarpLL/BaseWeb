package com.example.personalwebsite.model.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class AboutResponse {
    private ProfileInfo profile;
    private AboutContent about;

    @Data
    public static class ProfileInfo {
        private String name;
        private String title;
        private String bio;
        private String avatar;
    }

    @Data
    public static class AboutContent {
        private List<Skill> skills;
        private List<TimelineItem> timeline;
        private List<Project> projects;
    }

    @Data
    public static class Skill {
        private String name;
        private int level;
    }

    @Data
    public static class TimelineItem {
        private String date;
        private String title;
        private String description;
        private String type;
    }

    @Data
    public static class Project {
        private String name;
        private String description;
        private String url;
        private List<String> techs;
    }
}