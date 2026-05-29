package com.example.personalwebsite.controller.auth;

import com.example.personalwebsite.model.dto.request.LoginRequest;
import com.example.personalwebsite.model.dto.request.RegisterRequest;
import com.example.personalwebsite.model.dto.response.ApiResponse;
import com.example.personalwebsite.model.dto.response.LoginResponse;
import com.example.personalwebsite.model.entity.User;
import com.example.personalwebsite.security.CustomUserDetailsService;
import com.example.personalwebsite.security.JwtTokenProvider;
import com.example.personalwebsite.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager,
                         JwtTokenProvider jwtTokenProvider,
                         UserService userService,
                         PasswordEncoder passwordEncoder,
                         CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());
        if (user == null) {
            return ApiResponse.error("Invalid username or password");
        }
        if (userService.isAccountLocked(user)) {
            return ApiResponse.error("Account is locked");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            userService.updateLoginAttempts(user, user.getLoginAttempts() + 1,
                    user.getLoginAttempts() >= 4 ? LocalDateTime.now().plusMinutes(30) : null);
            return ApiResponse.error("Invalid username or password");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        userService.updateLoginAttempts(user, 0, null);
        userService.updateLastLogin(user.getId(), LocalDateTime.now());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setExpiresIn(jwtTokenProvider.getExpirationInSeconds());

        log.info("User {} logged in successfully", user.getUsername());
        return ApiResponse.success(response);
    }

    @PostMapping("/register")
    public ApiResponse<User> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.createUser(
                    request.getUsername(),
                    request.getPassword(),
                    "USER"
            );
            log.info("New user registered: {}", user.getUsername());
            return ApiResponse.success("Registration successful", user);
        } catch (Exception e) {
            log.warn("Registration failed: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        SecurityContextHolder.clearContext();
        return ApiResponse.success("Logout successful", null);
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ApiResponse.error("Not authenticated");
        }
        
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        String token = jwtTokenProvider.generateToken(authentication);
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setExpiresIn(jwtTokenProvider.getExpirationInSeconds());

        return ApiResponse.success(response);
    }
}