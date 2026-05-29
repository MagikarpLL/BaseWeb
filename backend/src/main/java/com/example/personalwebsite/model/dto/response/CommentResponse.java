package com.example.personalwebsite.model.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponse {
    private Long id;
    private Long postId;
    private String authorName;
    private String authorEmail;
    private String content;
    private Long parentId;
    private String status;
    private LocalDateTime createdAt;
    private List<CommentResponse> children;
    private String avatar; // Gravatar URL
}