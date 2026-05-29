package com.example.personalwebsite.service.impl;

import com.example.personalwebsite.exception.BadRequestException;
import com.example.personalwebsite.exception.ResourceNotFoundException;
import com.example.personalwebsite.mapper.UserMapper;
import com.example.personalwebsite.model.entity.User;
import com.example.personalwebsite.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User", "username", username);
        }
        return user;
    }

    @Override
    @Transactional
    public User createUser(String username, String password, String role) {
        if (username == null || username.trim().isEmpty()) {
            throw new BadRequestException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BadRequestException("Password cannot be empty");
        }

        User existingUser = userMapper.findByUsername(username);
        if (existingUser != null) {
            throw new BadRequestException("Username already exists: " + username);
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role != null ? role : "USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setLoginAttempts(0);

        userMapper.insert(user);
        log.info("Created new user: {}", username);
        return user;
    }

    @Override
    @Transactional
    public User updateLoginAttempts(User user, int attempts, LocalDateTime lockedUntil) {
        userMapper.updateLoginAttempts(user.getId(), attempts, lockedUntil, LocalDateTime.now());
        user.setLoginAttempts(attempts);
        user.setLockedUntil(lockedUntil);
        return user;
    }

    @Override
    @Transactional
    public User updateLastLogin(Long userId, LocalDateTime lastLoginAt) {
        User user = findById(userId);
        user.setLastLoginAt(lastLoginAt);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
        return user;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        User user = findById(id);
        userMapper.deleteById(id);
        log.info("Deleted user: {}", user.getUsername());
    }

    @Override
    public boolean isAccountLocked(User user) {
        if (user.getLockedUntil() == null) {
            return false;
        }
        return user.getLockedUntil().isAfter(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return userMapper.count();
    }
}