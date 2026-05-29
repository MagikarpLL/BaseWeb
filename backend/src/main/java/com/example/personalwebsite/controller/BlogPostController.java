package com.example.personalwebsite.controller;

import com.example.personalwebsite.model.dto.response.ApiResponse;
import com.example.personalwebsite.model.entity.BlogPost;
import com.example.personalwebsite.model.entity.Category;
import com.example.personalwebsite.model.entity.Tag;
import com.example.personalwebsite.service.BlogPostService;
import com.example.personalwebsite.service.CategoryService;
import com.example.personalwebsite.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/blog")
public class BlogPostController {

    private final BlogPostService blogPostService;
    private final CategoryService categoryService;
    private final TagService tagService;

    public BlogPostController(BlogPostService blogPostService,
                             CategoryService categoryService,
                             TagService tagService) {
        this.blogPostService = blogPostService;
        this.categoryService = categoryService;
        this.tagService = tagService;
    }

    /**
     * 获取文章列表（公开API）
     * GET /api/blog/posts?page=1&size=10&category=1&tag=1&sort=publishedAt
     */
    @GetMapping("/posts")
    public ApiResponse<Map<String, Object>> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Long tag,
            @RequestParam(defaultValue = "publishedAt") String sort) {

        // 计算偏移量
        int offset = (page - 1) * size;

        // 获取文章列表
        List<BlogPost> posts;
        if (category != null) {
            posts = blogPostService.findByCategoryId(category);
        } else if (tag != null) {
            posts = blogPostService.findByTagId(tag);
        } else {
            posts = blogPostService.findPublishedPostsPaginated(size, offset, sort);
        }

        // 获取总数
        long total = blogPostService.countPublished();

        // 获取分类和标签列表（用于筛选）
        List<Category> categories = categoryService.findAll();
        List<Tag> tags = tagService.findAll();

        // 构建响应
        Map<String, Object> data = new HashMap<>();
        data.put("posts", posts);
        data.put("pagination", Map.of(
                "page", page,
                "size", size,
                "total", total,
                "totalPages", (int) Math.ceil((double) total / size)
        ));
        data.put("filters", Map.of(
                "categories", categories.stream().map(c -> Map.of(
                        "id", c.getId(),
                        "name", c.getName(),
                        "slug", c.getSlug(),
                        "count", c.getPostCount() != null ? c.getPostCount() : 0
                )),
                "tags", tags.stream().map(t -> Map.of(
                        "id", t.getId(),
                        "name", t.getName(),
                        "slug", t.getSlug()
                ))
        ));

        return ApiResponse.success(data);
    }

    /**
     * 获取文章详情（公开API）
     * GET /api/blog/posts/:slug
     */
    @GetMapping("/posts/{slug}")
    public ApiResponse<BlogPost> getPostBySlug(@PathVariable String slug) {
        BlogPost post = blogPostService.findBySlug(slug);
        return ApiResponse.success(post);
    }

    /**
     * 增加阅读量（公开API）
     * POST /api/blog/posts/:slug/view
     */
    @PostMapping("/posts/{slug}/view")
    public ApiResponse<Void> incrementViewCount(@PathVariable String slug) {
        BlogPost post = blogPostService.findBySlug(slug);
        blogPostService.incrementReadCount(post.getId());
        return ApiResponse.success(null);
    }

    /**
     * 获取相关文章（使用多维度评分算法）
     * GET /api/blog/posts/{slug}/related
     */
    @GetMapping("/posts/{slug}/related")
    public ApiResponse<List<BlogPost>> getRelatedPosts(@PathVariable String slug) {
        BlogPost post = blogPostService.findBySlug(slug);
        List<Long> tagIds = post.getTagIds() != null ? post.getTagIds() : List.of();
        List<BlogPost> relatedPosts = blogPostService.findRelatedPostsWithScoring(
                post.getCategoryId(), post.getId(), 3, tagIds);
        return ApiResponse.success(relatedPosts);
    }

    /**
     * 搜索文章（公开API）
     * GET /api/blog/search?q={keyword}&page=1&size=10
     */
    @GetMapping("/search")
    public ApiResponse<Map<String, Object>> searchPosts(
            @RequestParam String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        int offset = (page - 1) * size;
        List<BlogPost> posts = blogPostService.searchPostsPaginated(q, size, offset);
        long total = blogPostService.countSearchResults(q);

        Map<String, Object> data = new HashMap<>();
        data.put("posts", posts);
        data.put("keyword", q);
        data.put("pagination", Map.of(
                "page", page,
                "size", size,
                "total", total,
                "totalPages", (int) Math.ceil((double) total / size)
        ));

        return ApiResponse.success(data);
    }

    /**
     * 获取公开标签列表
     * GET /api/blog/tags
     */
    @GetMapping("/tags")
    public ApiResponse<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.findAll();
        return ApiResponse.success(tags);
    }
}