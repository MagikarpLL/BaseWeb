package com.example.personalwebsite.service.impl;

import com.example.personalwebsite.exception.BadRequestException;
import com.example.personalwebsite.model.entity.CaptchaCode;
import com.example.personalwebsite.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int CODE_LENGTH = 4;
    
    // Store captchas in memory (sessionId -> CaptchaCode)
    // In production, consider using Redis or other distributed cache
    private final Map<String, CaptchaCode> captchaStore = new ConcurrentHashMap<>();

    @Override
    public CaptchaCode generateCaptcha(String sessionId) {
        String code = generateCode();
        CaptchaCode captcha = new CaptchaCode();
        captcha.setCode(code);
        captcha.setSessionId(sessionId);
        captcha.setCreatedAt(LocalDateTime.now());
        captcha.setAttempts(0);
        
        captchaStore.put(sessionId, captcha);
        log.debug("Generated captcha for session: {}", sessionId);
        
        return captcha;
    }

    @Override
    public boolean validateCaptcha(String sessionId, String code) {
        CaptchaCode storedCaptcha = getCaptcha(sessionId);
        
        if (storedCaptcha == null) {
            log.warn("Captcha not found for session: {}", sessionId);
            return false;
        }
        
        if (storedCaptcha.isExpired()) {
            log.warn("Captcha expired for session: {}", sessionId);
            captchaStore.remove(sessionId);
            return false;
        }
        
        if (storedCaptcha.getAttempts() >= 5) {
            log.warn("Too many captcha attempts for session: {}", sessionId);
            captchaStore.remove(sessionId);
            return false;
        }
        
        storedCaptcha.incrementAttempts();
        
        boolean valid = storedCaptcha.getCode().equalsIgnoreCase(code);
        if (valid) {
            captchaStore.remove(sessionId);
            log.debug("Captcha validated successfully for session: {}", sessionId);
        } else {
            log.debug("Captcha validation failed for session: {}", sessionId);
        }
        
        return valid;
    }

    @Override
    public CaptchaCode getCaptcha(String sessionId) {
        return captchaStore.get(sessionId);
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = (int) (Math.random() * CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString();
    }
}