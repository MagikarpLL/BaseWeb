package com.example.personalwebsite.model.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Comment {
    private Long id;
    private Long postId;
    private String authorName;
    private String authorEmail;
    private String content;
    private Long parentId;
    private String status;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Comment> children;
}