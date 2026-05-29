package com.example.personalwebsite.model.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BlogPost {
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private String coverImage;
    private String status;
    private Boolean isTop;
    private Integer readingTime;
    private Integer readCount;
    private Long authorId;
    private Long categoryId;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private List<Long> tagIds;
}