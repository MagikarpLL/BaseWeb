package com.example.personalwebsite.service;

import com.example.personalwebsite.model.entity.User;
import java.time.LocalDateTime;

public interface UserService {
    User findById(Long id);
    User findByUsername(String username);
    User createUser(String username, String password, String role);
    User updateLoginAttempts(User user, int attempts, LocalDateTime lockedUntil);
    User updateLastLogin(Long userId, LocalDateTime lastLoginAt);
    void deleteById(Long id);
    boolean isAccountLocked(User user);
    long count();
}