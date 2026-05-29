package com.example.personalwebsite.service;

import com.example.personalwebsite.model.entity.BlogPost;
import java.util.List;

public interface BlogPostService {
    BlogPost findById(Long id);
    BlogPost findBySlug(String slug);
    List<BlogPost> findByStatus(String status);
    List<BlogPost> findAll();
    List<BlogPost> searchPosts(String keyword);
    List<BlogPost> findByCategoryId(Long categoryId);
    List<BlogPost> findLatestPosts(Integer limit);
    List<BlogPost> findRelatedPosts(Long categoryId, Long excludeId, Integer limit);
    List<BlogPost> findPopularPosts(Integer limit);
    List<BlogPost> findByTagId(Long tagId);
    List<BlogPost> findPublishedPostsPaginated(int limit, int offset, String sort);
    void incrementReadCount(Long id);
    BlogPost create(BlogPost blogPost);
    BlogPost update(BlogPost blogPost);
    void softDelete(Long id);
    void deleteById(Long id);
    long countPublished();
    long sumReadCount();
    void publishPost(Long id);
    void draftPost(Long id);
    void toggleTop(Long id);
}