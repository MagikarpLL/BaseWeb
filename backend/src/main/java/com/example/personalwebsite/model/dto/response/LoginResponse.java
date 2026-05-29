package com.example.personalwebsite.model.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private String role;
    private Long expiresIn;
}