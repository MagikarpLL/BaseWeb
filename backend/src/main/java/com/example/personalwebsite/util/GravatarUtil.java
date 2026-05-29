package com.example.personalwebsite.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GravatarUtil {

    private static final String GRAVATAR_URL = "https://www.gravatar.com/avatar/%s?s=%d&d=identicon&r=g";
    
    /**
     * Generate Gravatar URL from email
     * @param email The email address
     * @param size The size of the avatar (1-2048)
     * @return The Gravatar URL
     */
    public static String getGravatarUrl(String email, int size) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        String hash = md5Hex(email.trim().toLowerCase());
        return String.format(GRAVATAR_URL, hash, size);
    }
    
    /**
     * Calculate MD5 hash of email in hex format
     */
    private static String md5Hex(String email) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(email.getBytes());
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            String hexChar = Integer.toHexString(0xff & b);
            if (hexChar.length() == 1) {
                hex.append('0');
            }
            hex.append(hexChar);
        }
        return hex.toString();
    }
}