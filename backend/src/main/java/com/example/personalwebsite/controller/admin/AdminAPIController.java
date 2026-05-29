package com.example.personalwebsite.controller.admin;

import com.example.personalwebsite.model.dto.request.BlogPostRequest;
import com.example.personalwebsite.model.dto.request.CategoryRequest;
import com.example.personalwebsite.model.dto.request.TagRequest;
import com.example.personalwebsite.model.dto.response.ApiResponse;
import com.example.personalwebsite.model.entity.BlogPost;
import com.example.personalwebsite.model.entity.Category;
import com.example.personalwebsite.model.entity.Tag;
import com.example.personalwebsite.service.BlogPostService;
import com.example.personalwebsite.service.CategoryService;
import com.example.personalwebsite.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminAPIController {

    private final BlogPostService blogPostService;
    private final CategoryService categoryService;
    private final TagService tagService;

    public AdminAPIController(BlogPostService blogPostService,
                             CategoryService categoryService,
                             TagService tagService) {
        this.blogPostService = blogPostService;
        this.categoryService = categoryService;
        this.tagService = tagService;
    }

    // Blog Post Management
    @GetMapping("/posts")
    public ApiResponse<List<BlogPost>> getAllPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<BlogPost> posts;
        if (keyword != null && !keyword.isEmpty()) {
            posts = blogPostService.searchPosts(keyword);
        } else {
            posts = blogPostService.findAll();
        }
        return ApiResponse.success(posts);
    }

    @GetMapping("/posts/{id}")
    public ApiResponse<BlogPost> getPostById(@PathVariable Long id) {
        BlogPost post = blogPostService.findById(id);
        return ApiResponse.success(post);
    }

    @PostMapping("/posts")
    public ApiResponse<BlogPost> createPost(@RequestBody BlogPostRequest request) {
        BlogPost post = new BlogPost();
        post.setTitle(request.getTitle());
        post.setSlug(request.getSlug());
        post.setExcerpt(request.getExcerpt());
        post.setContent(request.getContent());
        post.setCoverImage(request.getCoverImage());
        post.setStatus(request.getStatus());
        post.setIsTop(request.getIsTop());
        post.setAuthorId(request.getAuthorId());
        post.setCategoryId(request.getCategoryId());
        post.setPublishedAt(request.getPublishedAt());
        BlogPost created = blogPostService.create(post);
        return ApiResponse.success("Post created successfully", created);
    }

    @PutMapping("/posts/{id}")
    public ApiResponse<BlogPost> updatePost(@PathVariable Long id, @RequestBody BlogPostRequest request) {
        BlogPost post = blogPostService.findById(id);
        post.setTitle(request.getTitle());
        post.setSlug(request.getSlug());
        post.setExcerpt(request.getExcerpt());
        post.setContent(request.getContent());
        post.setCoverImage(request.getCoverImage());
        post.setStatus(request.getStatus());
        post.setIsTop(request.getIsTop());
        post.setCategoryId(request.getCategoryId());
        post.setPublishedAt(request.getPublishedAt());
        BlogPost updated = blogPostService.update(post);
        return ApiResponse.success("Post updated successfully", updated);
    }

    @DeleteMapping("/posts/{id}")
    public ApiResponse<Void> deletePost(@PathVariable Long id) {
        blogPostService.softDelete(id);
        return ApiResponse.success("Post deleted successfully", null);
    }

    @PostMapping("/posts/{id}/publish")
    public ApiResponse<Void> publishPost(@PathVariable Long id) {
        blogPostService.publishPost(id);
        return ApiResponse.success("Post published successfully", null);
    }

    @PostMapping("/posts/{id}/draft")
    public ApiResponse<Void> draftPost(@PathVariable Long id) {
        blogPostService.draftPost(id);
        return ApiResponse.success("Post reverted to draft successfully", null);
    }

    @PostMapping("/posts/{id}/top")
    public ApiResponse<Void> toggleTop(@PathVariable Long id) {
        blogPostService.toggleTop(id);
        return ApiResponse.success("Top status toggled successfully", null);
    }

    // Category Management
    @GetMapping("/categories")
    public ApiResponse<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.findAll();
        return ApiResponse.success(categories);
    }

    @PostMapping("/categories")
    public ApiResponse<Category> createCategory(@RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setSort(request.getSort());
        Category created = categoryService.create(category);
        return ApiResponse.success("Category created successfully", created);
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<Category> updateCategory(@PathVariable Long id, @RequestBody CategoryRequest request) {
        Category category = categoryService.findById(id);
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setSort(request.getSort());
        Category updated = categoryService.update(category);
        return ApiResponse.success("Category updated successfully", updated);
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteById(id);
        return ApiResponse.success("Category deleted successfully", null);
    }

    // Tag Management
    @GetMapping("/tags")
    public ApiResponse<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.findAll();
        return ApiResponse.success(tags);
    }

    @PostMapping("/tags")
    public ApiResponse<Tag> createTag(@RequestBody TagRequest request) {
        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setSlug(request.getSlug());
        Tag created = tagService.create(tag);
        return ApiResponse.success("Tag created successfully", created);
    }

    @PutMapping("/tags/{id}")
    public ApiResponse<Tag> updateTag(@PathVariable Long id, @RequestBody TagRequest request) {
        Tag tag = tagService.findById(id);
        tag.setName(request.getName());
        tag.setSlug(request.getSlug());
        Tag updated = tagService.update(tag);
        return ApiResponse.success("Tag updated successfully", updated);
    }

    @DeleteMapping("/tags/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteById(id);
        return ApiResponse.success("Tag deleted successfully", null);
    }
}