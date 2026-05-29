package com.example.personalwebsite.service;

import com.example.personalwebsite.model.entity.Tag;
import java.util.List;

public interface TagService {
    Tag findById(Long id);
    Tag findBySlug(String slug);
    List<Tag> findAll();
    List<Tag> findByPostId(Long postId);
    List<Tag> findPopularTags(Integer limit);
    Tag create(Tag tag);
    Tag update(Tag tag);
    void deleteById(Long id);
    void updatePostTags(Long postId, List<Long> tagIds);
}