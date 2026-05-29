package com.example.personalwebsite.service.impl;

import com.example.personalwebsite.exception.BadRequestException;
import com.example.personalwebsite.exception.ResourceNotFoundException;
import com.example.personalwebsite.mapper.CategoryMapper;
import com.example.personalwebsite.model.entity.Category;
import com.example.personalwebsite.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Category findById(Long id) {
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new ResourceNotFoundException("Category", "id", id);
        }
        return category;
    }

    @Override
    @Transactional(readOnly = true)
    public Category findBySlug(String slug) {
        Category category = categoryMapper.findBySlug(slug);
        if (category == null) {
            throw new ResourceNotFoundException("Category", "slug", slug);
        }
        return category;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryMapper.findAll();
    }

    @Override
    @Transactional
    public Category create(Category category) {
        validateCategory(category);

        Category existing = categoryMapper.findBySlug(category.getSlug());
        if (existing != null) {
            throw new BadRequestException("Category with slug already exists: " + category.getSlug());
        }

        LocalDateTime now = LocalDateTime.now();
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        if (category.getSort() == null) {
            category.setSort(0);
        }

        categoryMapper.insert(category);
        log.info("Created category: {} (slug: {})", category.getName(), category.getSlug());
        return category;
    }

    @Override
    @Transactional
    public Category update(Category category) {
        Category existing = findById(category.getId());
        validateCategory(category);

        if (!existing.getSlug().equals(category.getSlug())) {
            Category duplicate = categoryMapper.findBySlug(category.getSlug());
            if (duplicate != null && !duplicate.getId().equals(category.getId())) {
                throw new BadRequestException("Category with slug already exists: " + category.getSlug());
            }
        }

        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.update(category);
        log.info("Updated category: {} (id: {})", category.getName(), category.getId());
        return category;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Category category = findById(id);
        categoryMapper.deleteById(id);
        log.info("Deleted category: {} (id: {})", category.getName(), id);
    }

    private void validateCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new BadRequestException("Category name cannot be empty");
        }
        if (category.getSlug() == null || category.getSlug().trim().isEmpty()) {
            throw new BadRequestException("Category slug cannot be empty");
        }
    }
}