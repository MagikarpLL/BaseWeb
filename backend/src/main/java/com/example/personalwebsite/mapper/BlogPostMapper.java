package com.example.personalwebsite.mapper;

import com.example.personalwebsite.model.entity.BlogPost;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BlogPostMapper {

    @Select("SELECT * FROM blog_post WHERE id = #{id} AND deleted_at IS NULL")
    BlogPost findById(@Param("id") Long id);

    @Select("SELECT * FROM blog_post WHERE slug = #{slug} AND deleted_at IS NULL")
    BlogPost findBySlug(@Param("slug") String slug);

    @Select("SELECT * FROM blog_post WHERE status = #{status} AND deleted_at IS NULL ORDER BY published_at DESC")
    List<BlogPost> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM blog_post WHERE deleted_at IS NULL ORDER BY created_at DESC")
    List<BlogPost> findAll();

    @Select("SELECT * FROM blog_post WHERE deleted_at IS NULL AND title LIKE '%' || #{keyword} || '%' ORDER BY created_at DESC")
    List<BlogPost> searchPosts(@Param("keyword") String keyword);

    @Select("SELECT * FROM blog_post WHERE status = 'published' AND deleted_at IS NULL AND (title ILIKE '%' || #{keyword} || '%' OR content ILIKE '%' || #{keyword} || '%') ORDER BY published_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<BlogPost> searchPostsPaginated(@Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM blog_post WHERE status = 'published' AND deleted_at IS NULL AND (title ILIKE '%' || #{keyword} || '%' OR content ILIKE '%' || #{keyword} || '%')")
    long countSearchResults(@Param("keyword") String keyword);

    @Select("SELECT * FROM blog_post WHERE category_id = #{categoryId} AND status = 'published' AND deleted_at IS NULL")
    List<BlogPost> findByCategoryId(@Param("categoryId") Long categoryId);

    @Insert("INSERT INTO blog_post(title, slug, excerpt, content, cover_image, status, is_top, reading_time, read_count, " +
            "author_id, category_id, published_at, created_at, updated_at) " +
            "VALUES(#{title}, #{slug}, #{excerpt}, #{content}, #{coverImage}, #{status}, #{isTop}, #{readingTime}, #{readCount}, " +
            "#{authorId}, #{categoryId}, #{publishedAt}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BlogPost blogPost);

    @Update("UPDATE blog_post SET title = #{title}, slug = #{slug}, excerpt = #{excerpt}, content = #{content}, " +
            "cover_image = #{coverImage}, status = #{status}, is_top = #{isTop}, reading_time = #{readingTime}, read_count = #{readCount}, " +
            "category_id = #{categoryId}, published_at = #{publishedAt}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int update(BlogPost blogPost);

    @Update("UPDATE blog_post SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") java.time.LocalDateTime deletedAt);

    @Delete("DELETE FROM blog_post WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM blog_post WHERE status = 'published' AND deleted_at IS NULL")
    long countPublished();

    @Select("SELECT * FROM blog_post WHERE status = 'published' AND deleted_at IS NULL ORDER BY published_at DESC LIMIT #{limit}")
    List<BlogPost> findLatestPosts(@Param("limit") Integer limit);

    @Select("SELECT * FROM blog_post WHERE status = 'published' AND deleted_at IS NULL AND category_id = #{categoryId} AND id != #{excludeId} ORDER BY published_at DESC LIMIT #{limit}")
    List<BlogPost> findRelatedPosts(@Param("categoryId") Long categoryId, @Param("excludeId") Long excludeId, @Param("limit") Integer limit);

    /**
     * Find related posts using multi-dimensional scoring algorithm.
     * Scoring:
     *   - Same category: +3
     *   - Same tag (per tag): +2
     *   - Published within 7 days: +1
     *   - Higher read count: +0.01 per read (normalized)
     */
    @Select("""
        SELECT p.* FROM blog_post p
        LEFT JOIN blog_post_tag bpt ON p.id = bpt.post_id
        WHERE p.status = 'published'
          AND p.deleted_at IS NULL
          AND p.id != #{excludeId}
        GROUP BY p.id
        ORDER BY
          CASE WHEN p.category_id = #{categoryId} THEN 3 ELSE 0 END +
          COALESCE((SELECT COUNT(*) FROM blog_post_tag bpt2 WHERE bpt2.post_id = p.id AND bpt2.tag_id IN (<foreach collection='tagIds' item='tag' separator=','>#{tag}</foreach>)), 0) * 2 +
          CASE WHEN p.published_at > NOW() - INTERVAL '7 days' THEN 1 ELSE 0 END +
          LEAST(p.read_count * 0.01, 5)
        DESC
        LIMIT #{limit}
        """)
    List<BlogPost> findRelatedPostsWithScoring(@Param("categoryId") Long categoryId, @Param("excludeId") Long excludeId, @Param("limit") Integer limit, @Param("tagIds") List<Long> tagIds);

    @Select("SELECT * FROM blog_post WHERE status = 'published' AND deleted_at IS NULL ORDER BY read_count DESC LIMIT #{limit}")
    List<BlogPost> findPopularPosts(@Param("limit") Integer limit);

    @Select("SELECT bp.* FROM blog_post bp INNER JOIN blog_post_tag bpt ON bp.id = bpt.post_id WHERE bpt.tag_id = #{tagId} AND bp.status = 'published' AND bp.deleted_at IS NULL ORDER BY bp.published_at DESC")
    List<BlogPost> findByTagId(@Param("tagId") Long tagId);

    @Select("SELECT * FROM blog_post WHERE status = 'published' AND deleted_at IS NULL ORDER BY published_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<BlogPost> findPublishedPostsPaginated(@Param("limit") int limit, @Param("offset") int offset, @Param("sort") String sort);

    @Update("UPDATE blog_post SET read_count = read_count + 1 WHERE id = #{id}")
    int incrementReadCount(@Param("id") Long id);

    @Update("UPDATE blog_post SET status = #{status}, published_at = #{publishedAt} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("publishedAt") java.time.LocalDateTime publishedAt);

    @Update("UPDATE blog_post SET is_top = #{isTop} WHERE id = #{id}")
    int updateTop(@Param("id") Long id, @Param("isTop") Boolean isTop);

    @Select("SELECT COALESCE(SUM(read_count), 0) FROM blog_post WHERE status = 'published' AND deleted_at IS NULL")
    long sumReadCount();
}