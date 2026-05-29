package com.example.personalwebsite.controller;

import com.example.personalwebsite.model.dto.response.ApiResponse;
import com.example.personalwebsite.model.entity.CaptchaCode;
import com.example.personalwebsite.service.CaptchaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CaptchaController {

    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> generateCaptcha(HttpSession session) {
        String sessionId = session.getId();
        CaptchaCode captcha = captchaService.generateCaptcha(sessionId);
        
        CaptchaResponse response = new CaptchaResponse();
        response.setCaptchaId(sessionId);
        // In production, you might want to return just an ID and generate the image client-side
        // or serve the image directly. Here we return the code for simplicity in debugging.
        response.setCode(captcha.getCode()); // Remove this in production!
        
        log.debug("Generated captcha for session: {}", sessionId);
        return ApiResponse.success(response);
    }

    @PostMapping("/captcha/validate")
    public ApiResponse<Boolean> validateCaptcha(
            @RequestBody CaptchaValidationRequest request,
            HttpSession session) {
        String sessionId = session.getId();
        boolean valid = captchaService.validateCaptcha(sessionId, request.getCode());
        return ApiResponse.success(valid);
    }

    @lombok.Data
    public static class CaptchaResponse {
        private String captchaId;
        private String code; // Remove this in production - for debugging only
    }

    @lombok.Data
    public static class CaptchaValidationRequest {
        private String code;
    }
}