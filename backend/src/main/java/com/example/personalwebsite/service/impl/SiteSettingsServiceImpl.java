package com.example.personalwebsite.service.impl;

import com.example.personalwebsite.exception.BadRequestException;
import com.example.personalwebsite.exception.ResourceNotFoundException;
import com.example.personalwebsite.mapper.SiteSettingsMapper;
import com.example.personalwebsite.model.dto.SiteSettingsDTO;
import com.example.personalwebsite.model.entity.SiteSettings;
import com.example.personalwebsite.service.SiteSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SiteSettingsServiceImpl implements SiteSettingsService {

    private final SiteSettingsMapper siteSettingsMapper;

    public SiteSettingsServiceImpl(SiteSettingsMapper siteSettingsMapper) {
        this.siteSettingsMapper = siteSettingsMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public SiteSettings findById(Long id) {
        SiteSettings setting = siteSettingsMapper.findById(id);
        if (setting == null) {
            throw new ResourceNotFoundException("SiteSettings", "id", id);
        }
        return setting;
    }

    @Override
    @Transactional(readOnly = true)
    public SiteSettings findByKey(String settingKey) {
        SiteSettings setting = siteSettingsMapper.findByKey(settingKey);
        if (setting == null) {
            return null; // Return null instead of throwing exception for key not found
        }
        return setting;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SiteSettings> findAll() {
        return siteSettingsMapper.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> findAllAsMap() {
        List<SiteSettings> settings = siteSettingsMapper.findAll();
        Map<String, String> map = new HashMap<>();
        for (SiteSettings setting : settings) {
            map.put(setting.getSettingKey(), setting.getValue());
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public SiteSettingsDTO getSettingsDTO() {
        Map<String, String> map = findAllAsMap();
        return SiteSettingsDTO.fromMap(map);
    }

    @Override
    @Transactional
    public void updateFromDTO(SiteSettingsDTO dto) {
        Map<String, String> map = dto.toMap();
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            SiteSettings existing = siteSettingsMapper.findByKey(entry.getKey());
            if (existing != null) {
                existing.setValue(entry.getValue());
                existing.setUpdatedAt(now);
                siteSettingsMapper.update(existing);
            } else {
                SiteSettings newSetting = new SiteSettings();
                newSetting.setSettingKey(entry.getKey());
                newSetting.setValue(entry.getValue());
                newSetting.setType("string");
                newSetting.setCreatedAt(now);
                newSetting.setUpdatedAt(now);
                siteSettingsMapper.insert(newSetting);
            }
        }

        // Handle About section (nested arrays)
        if (dto.getAbout() != null) {
            if (dto.getAbout().getSkills() != null) {
                for (int i = 0; i < dto.getAbout().getSkills().size(); i++) {
                    SiteSettingsDTO.Skill skill = dto.getAbout().getSkills().get(i);
                    saveOrUpdateSetting("about.skills." + i + ".name", skill.getName(), now);
                    saveOrUpdateSetting("about.skills." + i + ".level", String.valueOf(skill.getLevel()), now);
                }
            }
            if (dto.getAbout().getTimeline() != null) {
                for (int i = 0; i < dto.getAbout().getTimeline().size(); i++) {
                    SiteSettingsDTO.TimelineItem item = dto.getAbout().getTimeline().get(i);
                    saveOrUpdateSetting("about.timeline." + i + ".date", item.getDate(), now);
                    saveOrUpdateSetting("about.timeline." + i + ".title", item.getTitle(), now);
                    saveOrUpdateSetting("about.timeline." + i + ".description", item.getDescription(), now);
                    saveOrUpdateSetting("about.timeline." + i + ".type", item.getType(), now);
                }
            }
            if (dto.getAbout().getProjects() != null) {
                for (int i = 0; i < dto.getAbout().getProjects().size(); i++) {
                    SiteSettingsDTO.Project project = dto.getAbout().getProjects().get(i);
                    saveOrUpdateSetting("about.projects." + i + ".name", project.getName(), now);
                    saveOrUpdateSetting("about.projects." + i + ".description", project.getDescription(), now);
                    saveOrUpdateSetting("about.projects." + i + ".url", project.getUrl(), now);
                    if (project.getTechs() != null) {
                        saveOrUpdateSetting("about.projects." + i + ".techs", String.join(",", project.getTechs()), now);
                    }
                }
            }
        }

        log.info("Updated site settings from DTO");
    }

    private void saveOrUpdateSetting(String key, String value, LocalDateTime now) {
        if (value == null) return;
        SiteSettings existing = siteSettingsMapper.findByKey(key);
        if (existing != null) {
            existing.setValue(value);
            existing.setUpdatedAt(now);
            siteSettingsMapper.update(existing);
        } else {
            SiteSettings newSetting = new SiteSettings();
            newSetting.setSettingKey(key);
            newSetting.setValue(value);
            newSetting.setType("string");
            newSetting.setCreatedAt(now);
            newSetting.setUpdatedAt(now);
            siteSettingsMapper.insert(newSetting);
        }
    }

    @Override
    @Transactional
    public SiteSettings create(SiteSettings siteSettings) {
        validateSiteSettings(siteSettings);

        SiteSettings existing = siteSettingsMapper.findByKey(siteSettings.getSettingKey());
        if (existing != null) {
            throw new BadRequestException("Setting with key already exists: " + siteSettings.getSettingKey());
        }

        LocalDateTime now = LocalDateTime.now();
        siteSettings.setCreatedAt(now);
        siteSettings.setUpdatedAt(now);

        siteSettingsMapper.insert(siteSettings);
        log.info("Created site setting: {} = {}", siteSettings.getSettingKey(), siteSettings.getValue());
        return siteSettings;
    }

    @Override
    @Transactional
    public SiteSettings update(SiteSettings siteSettings) {
        SiteSettings existing = findById(siteSettings.getId());
        validateSiteSettings(siteSettings);

        siteSettings.setUpdatedAt(LocalDateTime.now());
        siteSettingsMapper.update(siteSettings);
        log.info("Updated site setting: {} = {}", siteSettings.getSettingKey(), siteSettings.getValue());
        return siteSettings;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        SiteSettings setting = findById(id);
        siteSettingsMapper.deleteById(id);
        log.info("Deleted site setting: {} (id: {})", setting.getSettingKey(), id);
    }

    private void validateSiteSettings(SiteSettings siteSettings) {
        if (siteSettings.getSettingKey() == null || siteSettings.getSettingKey().trim().isEmpty()) {
            throw new BadRequestException("Setting key cannot be empty");
        }
        if (siteSettings.getValue() == null) {
            throw new BadRequestException("Setting value cannot be null");
        }
    }
}