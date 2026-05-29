package com.example.personalwebsite.model.dto.request;

import lombok.Data;

@Data
public class CommentRequest {
    private Long postId;
    private String authorName;
    private String authorEmail;
    private String content;
    private Long parentId;
}