package com.example.personalwebsite.controller.public_;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.personalwebsite.model.dto.SiteSettingsDTO;
import com.example.personalwebsite.model.dto.response.ApiResponse;
import com.example.personalwebsite.model.dto.response.HomeResponse;
import com.example.personalwebsite.model.entity.BlogPost;
import com.example.personalwebsite.model.entity.Category;
import com.example.personalwebsite.service.BlogPostService;
import com.example.personalwebsite.service.CategoryService;
import com.example.personalwebsite.service.SiteSettingsService;
import com.example.personalwebsite.service.TagService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublicAPIControllerTest {

    @Mock private BlogPostService blogPostService;
    @Mock private CategoryService categoryService;
    @Mock private TagService tagService;
    @Mock private SiteSettingsService siteSettingsService;

    private PublicAPIController controller;

    @BeforeEach
    void setUp() {
        controller =
                new PublicAPIController(
                        blogPostService, categoryService, tagService, siteSettingsService);
    }

    @Test
    void getHomeDataMapsProfileSocialLinksLatestPostsAndFeaturedTools() {
        SiteSettingsDTO settings = settings();
        BlogPost post = post();
        Category category = category();

        when(siteSettingsService.getSettingsDTO()).thenReturn(settings);
        when(blogPostService.findLatestPosts(3)).thenReturn(List.of(post));
        when(categoryService.findById(2L)).thenReturn(category);

        ApiResponse<HomeResponse> response = controller.getHomeData();

        assertThat(response.isSuccess()).isTrue();
        HomeResponse data = response.getData();
        assertThat(data.getProfile().getName()).isEqualTo("Alice");
        assertThat(data.getProfile().getTitle()).isEqualTo("Full-stack Engineer");
        assertThat(data.getProfile().getSocialLinks())
                .extracting(HomeResponse.SocialLink::getPlatform)
                .containsExactly("github", "email");
        assertThat(data.getProfile().getSocialLinks())
                .extracting(HomeResponse.SocialLink::getUrl)
                .containsExactly("https://github.com/alice", "mailto:alice@example.com");

        assertThat(data.getLatestPosts()).hasSize(1);
        assertThat(data.getLatestPosts().get(0).getTitle()).isEqualTo("Spec Driven Development");
        assertThat(data.getLatestPosts().get(0).getCategoryName()).isEqualTo("Engineering");
        assertThat(data.getFeaturedTools()).hasSize(3);
    }

    private static SiteSettingsDTO settings() {
        SiteSettingsDTO settings = new SiteSettingsDTO();
        SiteSettingsDTO.Profile profile = new SiteSettingsDTO.Profile();
        profile.setName("Alice");
        profile.setTitle("Full-stack Engineer");
        profile.setBio("Builds useful tools");
        profile.setAvatar("/avatar.png");
        settings.setProfile(profile);

        SiteSettingsDTO.Social social = new SiteSettingsDTO.Social();
        social.setGithub("https://github.com/alice");
        social.setEmail("alice@example.com");
        settings.setSocial(social);
        return settings;
    }

    private static BlogPost post() {
        BlogPost post = new BlogPost();
        post.setId(10L);
        post.setTitle("Spec Driven Development");
        post.setSlug("spec-driven-development");
        post.setExcerpt("Use specs to guide implementation");
        post.setReadCount(42);
        post.setCategoryId(2L);
        post.setPublishedAt(LocalDateTime.of(2026, 6, 8, 12, 0));
        return post;
    }

    private static Category category() {
        Category category = new Category();
        category.setId(2L);
        category.setName("Engineering");
        category.setSlug("engineering");
        return category;
    }
}
