package com.example.personalwebsite.mapper;

import com.example.personalwebsite.model.entity.SiteSettings;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SiteSettingsMapper {

    @Select("SELECT * FROM site_settings WHERE id = #{id}")
    SiteSettings findById(@Param("id") Long id);

    @Select("SELECT * FROM site_settings WHERE setting_key = #{settingKey}")
    SiteSettings findByKey(@Param("settingKey") String settingKey);

    @Select("SELECT * FROM site_settings ORDER BY id ASC")
    List<SiteSettings> findAll();

    @Insert("INSERT INTO site_settings(setting_key, value, type, created_at, updated_at) " +
            "VALUES(#{settingKey}, #{value}, #{type}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SiteSettings siteSettings);

    @Update("UPDATE site_settings SET value = #{value}, type = #{type}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(SiteSettings siteSettings);

    @Delete("DELETE FROM site_settings WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}