package com.example.personalwebsite.controller.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.personalwebsite.model.dto.request.LoginRequest;
import com.example.personalwebsite.model.dto.response.ApiResponse;
import com.example.personalwebsite.model.dto.response.LoginResponse;
import com.example.personalwebsite.model.entity.User;
import com.example.personalwebsite.security.CustomUserDetailsService;
import com.example.personalwebsite.security.JwtTokenProvider;
import com.example.personalwebsite.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserService userService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private Authentication authentication;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller =
                new AuthController(
                        authenticationManager,
                        jwtTokenProvider,
                        userService,
                        passwordEncoder,
                        userDetailsService);
    }

    @Test
    void loginReturnsTokenAndResetsAttemptsForValidCredentials() {
        User user = user("admin", "encoded-password", "ADMIN", 2);
        LoginRequest request = loginRequest("admin", "correct-password");

        when(userService.findByUsername("admin")).thenReturn(user);
        when(userService.isAccountLocked(user)).thenReturn(false);
        when(passwordEncoder.matches("correct-password", "encoded-password")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationInSeconds()).thenReturn(900L);

        ApiResponse<LoginResponse> response = controller.login(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getToken()).isEqualTo("jwt-token");
        assertThat(response.getData().getUsername()).isEqualTo("admin");
        assertThat(response.getData().getRole()).isEqualTo("ADMIN");
        assertThat(response.getData().getExpiresIn()).isEqualTo(900L);
        verify(userService).updateLoginAttempts(user, 0, null);
        verify(userService).updateLastLogin(eq(1L), any());
    }

    @Test
    void loginIncrementsAttemptsWhenPasswordIsInvalid() {
        User user = user("admin", "encoded-password", "ADMIN", 1);
        LoginRequest request = loginRequest("admin", "wrong-password");

        when(userService.findByUsername("admin")).thenReturn(user);
        when(userService.isAccountLocked(user)).thenReturn(false);
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        ApiResponse<LoginResponse> response = controller.login(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Invalid username or password");
        assertThat(response.getData()).isNull();
        verify(userService).updateLoginAttempts(user, 2, null);
        verify(authenticationManager, never()).authenticate(any());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    void loginRejectsLockedAccountBeforePasswordCheck() {
        User user = user("admin", "encoded-password", "ADMIN", 5);
        LoginRequest request = loginRequest("admin", "correct-password");

        when(userService.findByUsername("admin")).thenReturn(user);
        when(userService.isAccountLocked(user)).thenReturn(true);

        ApiResponse<LoginResponse> response = controller.login(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Account is locked");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(authenticationManager, never()).authenticate(any());
    }

    private static LoginRequest loginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private static User user(String username, String password, String role, int attempts) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setLoginAttempts(attempts);
        return user;
    }
}
