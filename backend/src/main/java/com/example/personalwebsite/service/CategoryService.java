package com.example.personalwebsite.service;

import com.example.personalwebsite.model.entity.Category;
import java.util.List;

public interface CategoryService {
    Category findById(Long id);
    Category findBySlug(String slug);
    List<Category> findAll();
    Category create(Category category);
    Category update(Category category);
    void deleteById(Long id);
}