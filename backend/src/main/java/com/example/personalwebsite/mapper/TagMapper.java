package com.example.personalwebsite.mapper;

import com.example.personalwebsite.model.entity.Tag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TagMapper {

    @Select("SELECT * FROM tag WHERE id = #{id}")
    Tag findById(@Param("id") Long id);

    @Select("SELECT * FROM tag WHERE slug = #{slug}")
    Tag findBySlug(@Param("slug") String slug);

    @Select("SELECT * FROM tag ORDER BY name ASC")
    List<Tag> findAll();

    @Select("SELECT t.* FROM tag t " +
            "INNER JOIN blog_post_tag bpt ON t.id = bpt.tag_id " +
            "WHERE bpt.post_id = #{postId}")
    List<Tag> findByPostId(@Param("postId") Long postId);

    @Insert("INSERT INTO tag(name, slug, created_at, updated_at) " +
            "VALUES(#{name}, #{slug}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Tag tag);

    @Update("UPDATE tag SET name = #{name}, slug = #{slug}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Tag tag);

    @Delete("DELETE FROM tag WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Insert("INSERT INTO blog_post_tag(post_id, tag_id) VALUES(#{postId}, #{tagId})")
    int insertPostTag(@Param("postId") Long postId, @Param("tagId") Long tagId);

    @Delete("DELETE FROM blog_post_tag WHERE post_id = #{postId}")
    int deletePostTags(@Param("postId") Long postId);

    @Select("SELECT t.* FROM tag t INNER JOIN blog_post_tag bpt ON t.id = bpt.tag_id INNER JOIN blog_post bp ON bpt.post_id = bp.id WHERE bp.status = 'published' AND bp.deleted_at IS NULL GROUP BY t.id ORDER BY COUNT(bpt.post_id) DESC LIMIT #{limit}")
    List<Tag> findPopularTags(@Param("limit") Integer limit);
}