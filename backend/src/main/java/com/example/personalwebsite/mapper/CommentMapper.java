package com.example.personalwebsite.mapper;

import com.example.personalwebsite.model.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Select("SELECT * FROM comment WHERE id = #{id}")
    Comment findById(@Param("id") Long id);

    @Select("SELECT * FROM comment WHERE post_id = #{postId} AND status = 'approved' ORDER BY created_at ASC")
    List<Comment> findByPostId(@Param("postId") Long postId);

    @Select("SELECT * FROM comment WHERE status = #{status} ORDER BY created_at DESC")
    List<Comment> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM comment ORDER BY created_at DESC")
    List<Comment> findAll();

    @Insert("INSERT INTO comment(post_id, author_name, author_email, content, parent_id, status, ip_address, user_agent, created_at, updated_at) " +
            "VALUES(#{postId}, #{authorName}, #{authorEmail}, #{content}, #{parentId}, #{status}, #{ipAddress}, #{userAgent}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    @Update("UPDATE comment SET author_name = #{authorName}, author_email = #{authorEmail}, content = #{content}, " +
            "status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Comment comment);

    @Update("UPDATE comment SET status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("updatedAt") java.time.LocalDateTime updatedAt);

    @Select("SELECT * FROM comment WHERE status = 'pending' ORDER BY created_at DESC")
    List<Comment> findPending();

    @Delete("DELETE FROM comment WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM comment")
    long countAll();
}