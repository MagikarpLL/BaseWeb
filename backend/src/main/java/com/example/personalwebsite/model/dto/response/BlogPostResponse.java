package com.example.personalwebsite.model.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BlogPostResponse {
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private String coverImage;
    private String status;
    private Integer readingTime;
    private Integer readCount;
    private Long authorId;
    private String authorUsername;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}