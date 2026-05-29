package com.example.personalwebsite.model.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class SiteSettingsDTO {
    private String siteName;
    private String siteDescription;
    private String seoTitle;
    private String seoDescription;
    private boolean allowComments;
    private boolean requireApproval;
    private Profile profile;
    private Social social;
    private About about;

    @Data
    public static class Profile {
        private String name;
        private String title;
        private String bio;
        private String avatar;
    }

    @Data
    public static class Social {
        private String github;
        private String twitter;
        private String email;
    }

    @Data
    public static class About {
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

    /**
     * Convert flat key-value map to nested DTO
     */
    public static SiteSettingsDTO fromMap(Map<String, String> map) {
        SiteSettingsDTO dto = new SiteSettingsDTO();
        dto.setSiteName(map.getOrDefault("siteName", ""));
        dto.setSiteDescription(map.getOrDefault("siteDescription", ""));
        dto.setSeoTitle(map.getOrDefault("seoTitle", ""));
        dto.setSeoDescription(map.getOrDefault("seoDescription", ""));
        dto.setAllowComments(Boolean.parseBoolean(map.getOrDefault("allowComments", "true")));
        dto.setRequireApproval(Boolean.parseBoolean(map.getOrDefault("requireApproval", "false")));

        Profile profile = new Profile();
        profile.setName(map.getOrDefault("profile.name", ""));
        profile.setTitle(map.getOrDefault("profile.title", ""));
        profile.setBio(map.getOrDefault("profile.bio", ""));
        profile.setAvatar(map.getOrDefault("profile.avatar", ""));
        dto.setProfile(profile);

        Social social = new Social();
        social.setGithub(map.getOrDefault("social.github", ""));
        social.setTwitter(map.getOrDefault("social.twitter", ""));
        social.setEmail(map.getOrDefault("social.email", ""));
        dto.setSocial(social);

        // Parse about section
        About about = new About();
        List<Skill> skills = new ArrayList<>();
        List<TimelineItem> timeline = new ArrayList<>();
        List<Project> projects = new ArrayList<>();

        // Skills: about.skills.0.name, about.skills.0.level, etc.
        for (int i = 0; i < 20; i++) {
            String name = map.get("about.skills." + i + ".name");
            if (name == null) break;
            Skill skill = new Skill();
            skill.setName(name);
            String levelStr = map.get("about.skills." + i + ".level");
            skill.setLevel(levelStr != null ? Integer.parseInt(levelStr) : 0);
            skills.add(skill);
        }

        // Timeline: about.timeline.0.date, about.timeline.0.title, etc.
        for (int i = 0; i < 20; i++) {
            String date = map.get("about.timeline." + i + ".date");
            if (date == null) break;
            TimelineItem item = new TimelineItem();
            item.setDate(date);
            item.setTitle(map.get("about.timeline." + i + ".title"));
            item.setDescription(map.get("about.timeline." + i + ".description"));
            item.setType(map.get("about.timeline." + i + ".type"));
            timeline.add(item);
        }

        // Projects: about.projects.0.name, about.projects.0.description, etc.
        for (int i = 0; i < 20; i++) {
            String name = map.get("about.projects." + i + ".name");
            if (name == null) break;
            Project project = new Project();
            project.setName(name);
            project.setDescription(map.get("about.projects." + i + ".description"));
            project.setUrl(map.get("about.projects." + i + ".url"));
            // techs: comma-separated list
            String techsStr = map.get("about.projects." + i + ".techs");
            if (techsStr != null && !techsStr.isEmpty()) {
                project.setTechs(java.util.Arrays.asList(techsStr.split(",")));
            } else {
                project.setTechs(new ArrayList<>());
            }
            projects.add(project);
        }

        about.setSkills(skills);
        about.setTimeline(timeline);
        about.setProjects(projects);
        dto.setAbout(about);

        return dto;
    }

    /**
     * Convert nested DTO to flat key-value map for storage
     */
    public Map<String, String> toMap() {
        java.util.HashMap<String, String> map = new java.util.HashMap<>();
        if (siteName != null) map.put("siteName", siteName);
        if (siteDescription != null) map.put("siteDescription", siteDescription);
        if (seoTitle != null) map.put("seoTitle", seoTitle);
        if (seoDescription != null) map.put("seoDescription", seoDescription);
        map.put("allowComments", String.valueOf(allowComments));
        map.put("requireApproval", String.valueOf(requireApproval));

        if (profile != null) {
            if (profile.getName() != null) map.put("profile.name", profile.getName());
            if (profile.getTitle() != null) map.put("profile.title", profile.getTitle());
            if (profile.getBio() != null) map.put("profile.bio", profile.getBio());
            if (profile.getAvatar() != null) map.put("profile.avatar", profile.getAvatar());
        }

        if (social != null) {
            if (social.getGithub() != null) map.put("social.github", social.getGithub());
            if (social.getTwitter() != null) map.put("social.twitter", social.getTwitter());
            if (social.getEmail() != null) map.put("social.email", social.getEmail());
        }

        return map;
    }
}
