package com.example.personalwebsite.mapper;

import com.example.personalwebsite.model.entity.PostHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostHistoryMapper {

    @Insert("INSERT INTO post_history(post_id, title, content, excerpt, status, created_at, created_by) " +
            "VALUES(#{postId}, #{title}, #{content}, #{excerpt}, #{status}, #{createdAt}, #{createdBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PostHistory history);

    @Select("SELECT * FROM post_history WHERE id = #{id}")
    PostHistory findById(@Param("id") Long id);

    @Select("SELECT * FROM post_history WHERE post_id = #{postId} ORDER BY created_at DESC")
    List<PostHistory> findByPostId(@Param("postId") Long postId);

    @Select("SELECT * FROM post_history WHERE post_id = #{postId} ORDER BY created_at DESC LIMIT 1")
    PostHistory findLatestByPostId(@Param("postId") Long postId);

    @Select("SELECT COUNT(*) FROM post_history WHERE post_id = #{postId}")
    long countByPostId(@Param("postId") Long postId);

    @Delete("DELETE FROM post_history WHERE post_id = #{postId} AND id NOT IN " +
            "(SELECT id FROM (SELECT id FROM post_history WHERE post_id = #{postId} ORDER BY created_at DESC LIMIT #{keepCount}) AS recent)")
    int deleteOldVersions(@Param("postId") Long postId, @Param("keepCount") int keepCount);

    @Delete("DELETE FROM post_history WHERE post_id = #{postId} AND id = #{id}")
    int deleteById(@Param("id") Long id);
}