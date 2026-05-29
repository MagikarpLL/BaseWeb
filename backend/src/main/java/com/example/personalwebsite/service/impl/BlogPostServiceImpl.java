package com.example.personalwebsite.service.impl;

import com.example.personalwebsite.exception.BadRequestException;
import com.example.personalwebsite.exception.ResourceNotFoundException;
import com.example.personalwebsite.mapper.BlogPostMapper;
import com.example.personalwebsite.mapper.TagMapper;
import com.example.personalwebsite.model.entity.BlogPost;
import com.example.personalwebsite.service.BlogPostService;
import com.example.personalwebsite.service.PostHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class BlogPostServiceImpl implements BlogPostService {

    private final BlogPostMapper blogPostMapper;
    private final TagMapper tagMapper;
    private final PostHistoryService postHistoryService;

    public BlogPostServiceImpl(BlogPostMapper blogPostMapper, TagMapper tagMapper, PostHistoryService postHistoryService) {
        this.blogPostMapper = blogPostMapper;
        this.tagMapper = tagMapper;
        this.postHistoryService = postHistoryService;
    }

    @Override
    @Transactional(readOnly = true)
    public BlogPost findById(Long id) {
        BlogPost post = blogPostMapper.findById(id);
        if (post == null) {
            throw new ResourceNotFoundException("BlogPost", "id", id);
        }
        return post;
    }

    @Override
    @Transactional(readOnly = true)
    public BlogPost findBySlug(String slug) {
        BlogPost post = blogPostMapper.findBySlug(slug);
        if (post == null) {
            throw new ResourceNotFoundException("BlogPost", "slug", slug);
        }
        return post;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPost> findByStatus(String status) {
        return blogPostMapper.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPost> findAll() {
        return blogPostMapper.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPost> searchPosts(String keyword) {
        return blogPostMapper.searchPosts(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPost> findByCategoryId(Long categoryId) {
        return blogPostMapper.findByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPost> findLatestPosts(Integer limit) {
        return blogPostMapper.findLatestPosts(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPost> findRelatedPosts(Long categoryId, Long excludeId, Integer limit) {
        return blogPostMapper.findRelatedPosts(categoryId, excludeId, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPost> findRelatedPostsWithScoring(Long categoryId, Long excludeId, Integer limit, List<Long> tagIds) {
        return blogPostMapper.findRelatedPostsWithScoring(categoryId, excludeId, limit, tagIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPost> findPopularPosts(Integer limit) {
        return blogPostMapper.findPopularPosts(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPost> findByTagId(Long tagId) {
        return blogPostMapper.findByTagId(tagId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPost> findPublishedPostsPaginated(int limit, int offset, String sort) {
        return blogPostMapper.findPublishedPostsPaginated(limit, offset, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPost> searchPostsPaginated(String keyword, int limit, int offset) {
        return blogPostMapper.searchPostsPaginated(keyword, limit, offset);
    }

    @Override
    @Transactional(readOnly = true)
    public long countSearchResults(String keyword) {
        return blogPostMapper.countSearchResults(keyword);
    }

    @Override
    @Transactional
    public void incrementReadCount(Long id) {
        blogPostMapper.incrementReadCount(id);
    }

    @Override
    @Transactional
    public void publishPost(Long id) {
        BlogPost post = findById(id);
        post.setStatus("published");
        post.setPublishedAt(LocalDateTime.now());
        blogPostMapper.updateStatus(id, "published", post.getPublishedAt());

        // Save history snapshot on publish
        postHistoryService.saveHistory(id, post.getAuthorId());

        log.info("Published blog post: {} (id: {})", post.getTitle(), id);
    }

    @Override
    @Transactional
    public void draftPost(Long id) {
        BlogPost post = findById(id);
        post.setStatus("draft");
        blogPostMapper.updateStatus(id, "draft", null);
        log.info("Reverted to draft: blog post {} (id: {})", post.getTitle(), id);
    }

    @Override
    @Transactional
    public void toggleTop(Long id) {
        BlogPost post = findById(id);
        post.setIsTop(!Boolean.TRUE.equals(post.getIsTop()));
        blogPostMapper.updateTop(id, post.getIsTop());
        log.info("Toggled top status: blog post {} (id: {}), isTop: {}", post.getTitle(), id, post.getIsTop());
    }

    @Override
    @Transactional
    public BlogPost create(BlogPost blogPost) {
        validateBlogPost(blogPost);

        BlogPost existing = blogPostMapper.findBySlug(blogPost.getSlug());
        if (existing != null) {
            throw new BadRequestException("Blog post with slug already exists: " + blogPost.getSlug());
        }

        LocalDateTime now = LocalDateTime.now();
        blogPost.setCreatedAt(now);
        blogPost.setUpdatedAt(now);

        if (blogPost.getReadCount() == null) {
            blogPost.setReadCount(0);
        }
        if (blogPost.getIsTop() == null) {
            blogPost.setIsTop(false);
        }
        if (blogPost.getReadingTime() == null) {
            blogPost.setReadingTime(calculateReadingTime(blogPost.getContent()));
        }

        blogPostMapper.insert(blogPost);

        if (blogPost.getTagIds() != null && !blogPost.getTagIds().isEmpty()) {
            for (Long tagId : blogPost.getTagIds()) {
                tagMapper.insertPostTag(blogPost.getId(), tagId);
            }
        }

        log.info("Created blog post: {} (slug: {})", blogPost.getTitle(), blogPost.getSlug());
        return blogPost;
    }

    @Override
    @Transactional
    public BlogPost update(BlogPost blogPost) {
        BlogPost existing = findById(blogPost.getId());
        validateBlogPost(blogPost);

        if (!existing.getSlug().equals(blogPost.getSlug())) {
            BlogPost duplicate = blogPostMapper.findBySlug(blogPost.getSlug());
            if (duplicate != null && !duplicate.getId().equals(blogPost.getId())) {
                throw new BadRequestException("Blog post with slug already exists: " + blogPost.getSlug());
            }
        }

        blogPost.setUpdatedAt(LocalDateTime.now());

        if (blogPost.getReadingTime() == null && blogPost.getContent() != null) {
            blogPost.setReadingTime(calculateReadingTime(blogPost.getContent()));
        }

        blogPostMapper.update(blogPost);

        if (blogPost.getTagIds() != null) {
            tagMapper.deletePostTags(blogPost.getId());
            for (Long tagId : blogPost.getTagIds()) {
                tagMapper.insertPostTag(blogPost.getId(), tagId);
            }
        }

        log.info("Updated blog post: {} (id: {})", blogPost.getTitle(), blogPost.getId());
        return blogPost;
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        BlogPost post = findById(id);
        post.setDeletedAt(LocalDateTime.now());
        blogPostMapper.softDelete(id, post.getDeletedAt());
        log.info("Soft deleted blog post: {} (id: {})", post.getTitle(), id);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        BlogPost post = findById(id);
        tagMapper.deletePostTags(id);
        blogPostMapper.deleteById(id);
        log.info("Permanently deleted blog post: {} (id: {})", post.getTitle(), id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPublished() {
        return blogPostMapper.countPublished();
    }

    @Override
    @Transactional(readOnly = true)
    public long sumReadCount() {
        return blogPostMapper.sumReadCount();
    }

    private void validateBlogPost(BlogPost blogPost) {
        if (blogPost.getTitle() == null || blogPost.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Title cannot be empty");
        }
        if (blogPost.getSlug() == null || blogPost.getSlug().trim().isEmpty()) {
            throw new BadRequestException("Slug cannot be empty");
        }
        if (blogPost.getContent() == null || blogPost.getContent().trim().isEmpty()) {
            throw new BadRequestException("Content cannot be empty");
        }
    }

    private int calculateReadingTime(String content) {
        if (content == null || content.isEmpty()) {
            return 1;
        }
        int wordCount = content.split("\\s+").length;
        return Math.max(1, wordCount / 200);
    }
}