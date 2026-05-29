package com.example.personalwebsite.mapper;

import com.example.personalwebsite.model.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM category WHERE id = #{id}")
    Category findById(@Param("id") Long id);

    @Select("SELECT * FROM category WHERE slug = #{slug}")
    Category findBySlug(@Param("slug") String slug);

    @Select("SELECT * FROM category ORDER BY sort ASC")
    List<Category> findAll();

    @Insert("INSERT INTO category(name, slug, description, sort, created_at, updated_at) " +
            "VALUES(#{name}, #{slug}, #{description}, #{sort}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE category SET name = #{name}, slug = #{slug}, description = #{description}, " +
            "sort = #{sort}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Category category);

    @Delete("DELETE FROM category WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}