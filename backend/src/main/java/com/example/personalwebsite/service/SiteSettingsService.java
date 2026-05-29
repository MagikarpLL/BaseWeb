package com.example.personalwebsite.service;

import com.example.personalwebsite.model.dto.SiteSettingsDTO;
import com.example.personalwebsite.model.entity.SiteSettings;
import java.util.List;
import java.util.Map;

public interface SiteSettingsService {
    SiteSettings findById(Long id);
    SiteSettings findByKey(String settingKey);
    List<SiteSettings> findAll();
    SiteSettings create(SiteSettings siteSettings);
    SiteSettings update(SiteSettings siteSettings);
    void deleteById(Long id);

    // Flat key-value operations
    Map<String, String> findAllAsMap();

    // Nested DTO operations
    SiteSettingsDTO getSettingsDTO();
    void updateFromDTO(SiteSettingsDTO dto);
}