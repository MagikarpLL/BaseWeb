package com.example.personalwebsite.service.impl;

import com.example.personalwebsite.exception.BadRequestException;
import com.example.personalwebsite.exception.ResourceNotFoundException;
import com.example.personalwebsite.mapper.TagMapper;
import com.example.personalwebsite.model.entity.Tag;
import com.example.personalwebsite.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    public TagServiceImpl(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Tag findById(Long id) {
        Tag tag = tagMapper.findById(id);
        if (tag == null) {
            throw new ResourceNotFoundException("Tag", "id", id);
        }
        return tag;
    }

    @Override
    @Transactional(readOnly = true)
    public Tag findBySlug(String slug) {
        Tag tag = tagMapper.findBySlug(slug);
        if (tag == null) {
            throw new ResourceNotFoundException("Tag", "slug", slug);
        }
        return tag;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> findAll() {
        return tagMapper.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> findByPostId(Long postId) {
        return tagMapper.findByPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> findPopularTags(Integer limit) {
        return tagMapper.findPopularTags(limit);
    }

    @Override
    @Transactional
    public Tag create(Tag tag) {
        validateTag(tag);

        Tag existing = tagMapper.findBySlug(tag.getSlug());
        if (existing != null) {
            throw new BadRequestException("Tag with slug already exists: " + tag.getSlug());
        }

        LocalDateTime now = LocalDateTime.now();
        tag.setCreatedAt(now);
        tag.setUpdatedAt(now);

        tagMapper.insert(tag);
        log.info("Created tag: {} (slug: {})", tag.getName(), tag.getSlug());
        return tag;
    }

    @Override
    @Transactional
    public Tag update(Tag tag) {
        Tag existing = findById(tag.getId());
        validateTag(tag);

        if (!existing.getSlug().equals(tag.getSlug())) {
            Tag duplicate = tagMapper.findBySlug(tag.getSlug());
            if (duplicate != null && !duplicate.getId().equals(tag.getId())) {
                throw new BadRequestException("Tag with slug already exists: " + tag.getSlug());
            }
        }

        tag.setUpdatedAt(LocalDateTime.now());
        tagMapper.update(tag);
        log.info("Updated tag: {} (id: {})", tag.getName(), tag.getId());
        return tag;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Tag tag = findById(id);
        tagMapper.deletePostTags(id);
        tagMapper.deleteById(id);
        log.info("Deleted tag: {} (id: {})", tag.getName(), id);
    }

    @Override
    @Transactional
    public void updatePostTags(Long postId, List<Long> tagIds) {
        if (postId == null) {
            throw new BadRequestException("Post ID cannot be null");
        }

        tagMapper.deletePostTags(postId);

        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                tagMapper.insertPostTag(postId, tagId);
            }
        }
        log.debug("Updated tags for post {}: {}", postId, tagIds);
    }

    private void validateTag(Tag tag) {
        if (tag.getName() == null || tag.getName().trim().isEmpty()) {
            throw new BadRequestException("Tag name cannot be empty");
        }
        if (tag.getSlug() == null || tag.getSlug().trim().isEmpty()) {
            throw new BadRequestException("Tag slug cannot be empty");
        }
    }
}