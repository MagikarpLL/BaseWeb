package com.example.personalwebsite.service;

import com.example.personalwebsite.model.entity.CaptchaCode;

public interface CaptchaService {
    CaptchaCode generateCaptcha(String sessionId);
    boolean validateCaptcha(String sessionId, String code);
    CaptchaCode getCaptcha(String sessionId);
}