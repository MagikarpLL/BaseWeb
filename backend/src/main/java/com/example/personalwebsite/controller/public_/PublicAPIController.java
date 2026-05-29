package com.example.personalwebsite.controller.public_;

import com.example.personalwebsite.model.dto.SiteSettingsDTO;
import com.example.personalwebsite.model.dto.response.AboutResponse;
import com.example.personalwebsite.model.dto.response.ApiResponse;
import com.example.personalwebsite.model.dto.response.HomeResponse;
import com.example.personalwebsite.model.entity.BlogPost;
import com.example.personalwebsite.model.entity.Category;
import com.example.personalwebsite.model.entity.Tag;
import com.example.personalwebsite.service.BlogPostService;
import com.example.personalwebsite.service.CategoryService;
import com.example.personalwebsite.service.SiteSettingsService;
import com.example.personalwebsite.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/public")
public class PublicAPIController {

    private final BlogPostService blogPostService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final SiteSettingsService siteSettingsService;

    public PublicAPIController(BlogPostService blogPostService,
                               CategoryService categoryService,
                               TagService tagService,
                               SiteSettingsService siteSettingsService) {
        this.blogPostService = blogPostService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.siteSettingsService = siteSettingsService;
    }

    @GetMapping("/home")
    public ApiResponse<HomeResponse> getHomeData() {
        HomeResponse response = new HomeResponse();

        // Build profile from site settings
        SiteSettingsDTO settings = siteSettingsService.getSettingsDTO();
        HomeResponse.ProfileInfo profile = new HomeResponse.ProfileInfo();
        if (settings.getProfile() != null) {
            profile.setName(settings.getProfile().getName());
            profile.setTitle(settings.getProfile().getTitle());
            profile.setBio(settings.getProfile().getBio());
            profile.setAvatar(settings.getProfile().getAvatar());
        }
        if (settings.getSocial() != null) {
            List<HomeResponse.SocialLink> socialLinks = new ArrayList<>();
            if (settings.getSocial().getGithub() != null && !settings.getSocial().getGithub().isEmpty()) {
                HomeResponse.SocialLink github = new HomeResponse.SocialLink();
                github.setPlatform("github");
                github.setUrl(settings.getSocial().getGithub());
                socialLinks.add(github);
            }
            if (settings.getSocial().getTwitter() != null && !settings.getSocial().getTwitter().isEmpty()) {
                HomeResponse.SocialLink twitter = new HomeResponse.SocialLink();
                twitter.setPlatform("twitter");
                twitter.setUrl(settings.getSocial().getTwitter());
                socialLinks.add(twitter);
            }
            if (settings.getSocial().getEmail() != null && !settings.getSocial().getEmail().isEmpty()) {
                HomeResponse.SocialLink email = new HomeResponse.SocialLink();
                email.setPlatform("email");
                email.setUrl("mailto:" + settings.getSocial().getEmail());
                socialLinks.add(email);
            }
            profile.setSocialLinks(socialLinks);
        }
        response.setProfile(profile);

        // Latest posts (3 most recent published)
        List<BlogPost> latestPosts = blogPostService.findLatestPosts(3);
        List<HomeResponse.PostSummary> postSummaries = new ArrayList<>();
        for (BlogPost post : latestPosts) {
            HomeResponse.PostSummary summary = new HomeResponse.PostSummary();
            summary.setId(post.getId());
            summary.setTitle(post.getTitle());
            summary.setSlug(post.getSlug());
            summary.setExcerpt(post.getExcerpt());
            summary.setReadCount(post.getReadCount());
            if (post.getPublishedAt() != null) {
                summary.setPublishedAt(post.getPublishedAt().toString());
            }
            if (post.getCategoryId() != null) {
                Category category = categoryService.findById(post.getCategoryId());
                if (category != null) {
                    summary.setCategoryName(category.getName());
                }
            }
            postSummaries.add(summary);
        }
        response.setLatestPosts(postSummaries);

        // Featured tools (hardcoded for now - can be extended to use DB)
        List<HomeResponse.ToolSummary> tools = new ArrayList<>();
        HomeResponse.ToolSummary tool1 = new HomeResponse.ToolSummary();
        tool1.setId(1L);
        tool1.setName("JSON 格式化");
        tool1.setIcon("code");
        tool1.setDescription("在线格式化、压缩和验证 JSON 数据");
        tool1.setPath("/tools/timestamp-converter");
        tools.add(tool1);

        HomeResponse.ToolSummary tool2 = new HomeResponse.ToolSummary();
        tool2.setId(2L);
        tool2.setName("UUID 生成器");
        tool2.setIcon("key");
        tool2.setDescription("快速生成 UUID/GUID");
        tool2.setPath("/tools/uuid-generator");
        tools.add(tool2);

        HomeResponse.ToolSummary tool3 = new HomeResponse.ToolSummary();
        tool3.setId(3L);
        tool3.setName("颜色转换器");
        tool3.setIcon("palette");
        tool3.setDescription("HEX、RGB、HSL 颜色格式互转");
        tool3.setPath("/tools/color-converter");
        tools.add(tool3);

        response.setFeaturedTools(tools);

        return ApiResponse.success(response);
    }

    @GetMapping("/about")
    public ApiResponse<AboutResponse> getAboutData() {
        AboutResponse response = new AboutResponse();

        // Build profile from site settings
        SiteSettingsDTO settings = siteSettingsService.getSettingsDTO();
        AboutResponse.ProfileInfo profile = new AboutResponse.ProfileInfo();
        if (settings.getProfile() != null) {
            profile.setName(settings.getProfile().getName());
            profile.setTitle(settings.getProfile().getTitle());
            profile.setBio(settings.getProfile().getBio());
            profile.setAvatar(settings.getProfile().getAvatar());
        }
        response.setProfile(profile);

        // Build about content from site settings
        AboutResponse.AboutContent about = new AboutResponse.AboutContent();
        if (settings.getAbout() != null) {
            // Convert SiteSettingsDTO nested types to AboutResponse nested types
            List<AboutResponse.Skill> skills = new ArrayList<>();
            if (settings.getAbout().getSkills() != null) {
                for (SiteSettingsDTO.Skill skill : settings.getAbout().getSkills()) {
                    AboutResponse.Skill s = new AboutResponse.Skill();
                    s.setName(skill.getName());
                    s.setLevel(skill.getLevel());
                    skills.add(s);
                }
            }
            List<AboutResponse.TimelineItem> timeline = new ArrayList<>();
            if (settings.getAbout().getTimeline() != null) {
                for (SiteSettingsDTO.TimelineItem item : settings.getAbout().getTimeline()) {
                    AboutResponse.TimelineItem t = new AboutResponse.TimelineItem();
                    t.setDate(item.getDate());
                    t.setTitle(item.getTitle());
                    t.setDescription(item.getDescription());
                    t.setType(item.getType());
                    timeline.add(t);
                }
            }
            List<AboutResponse.Project> projects = new ArrayList<>();
            if (settings.getAbout().getProjects() != null) {
                for (SiteSettingsDTO.Project project : settings.getAbout().getProjects()) {
                    AboutResponse.Project p = new AboutResponse.Project();
                    p.setName(project.getName());
                    p.setDescription(project.getDescription());
                    p.setUrl(project.getUrl());
                    p.setTechs(project.getTechs());
                    projects.add(p);
                }
            }
            about.setSkills(skills);
            about.setTimeline(timeline);
            about.setProjects(projects);
        } else {
            about.setSkills(new ArrayList<>());
            about.setTimeline(new ArrayList<>());
            about.setProjects(new ArrayList<>());
        }
        response.setAbout(about);

        return ApiResponse.success(response);
    }

    @GetMapping("/posts")
    public ApiResponse<List<BlogPost>> getPublishedPosts() {
        List<BlogPost> posts = blogPostService.findByStatus("published");
        return ApiResponse.success(posts);
    }

    @GetMapping("/posts/{slug}")
    public ApiResponse<BlogPost> getPostBySlug(@PathVariable String slug) {
        BlogPost post = blogPostService.findBySlug(slug);
        return ApiResponse.success(post);
    }

    @GetMapping("/categories")
    public ApiResponse<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.findAll();
        return ApiResponse.success(categories);
    }

    @GetMapping("/tags")
    public ApiResponse<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.findAll();
        return ApiResponse.success(tags);
    }
}