package com.example.personalwebsite.mapper;

import com.example.personalwebsite.model.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(@Param("id") Long id);

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Insert("INSERT INTO users(username, password, role, created_at, updated_at, login_attempts) " +
            "VALUES(#{username}, #{password}, #{role}, #{createdAt}, #{updatedAt}, #{loginAttempts})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE users SET password = #{password}, updated_at = #{updatedAt}, " +
            "login_attempts = #{loginAttempts}, locked_until = #{lockedUntil}, last_login_at = #{lastLoginAt} " +
            "WHERE id = #{id}")
    int update(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Update("UPDATE users SET login_attempts = #{attempts}, locked_until = #{lockedUntil}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateLoginAttempts(@Param("id") Long id, @Param("attempts") int attempts, @Param("lockedUntil") java.time.LocalDateTime lockedUntil, @Param("updatedAt") java.time.LocalDateTime updatedAt);

    @Select("SELECT COUNT(*) FROM users")
    long count();
}