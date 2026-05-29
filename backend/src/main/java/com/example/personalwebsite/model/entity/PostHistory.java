package com.example.personalwebsite.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostHistory {
    private Long id;
    private Long postId;
    private String title;
    private String content;
    private String excerpt;
    private String status;
    private LocalDateTime createdAt;
    private Long createdBy;
}