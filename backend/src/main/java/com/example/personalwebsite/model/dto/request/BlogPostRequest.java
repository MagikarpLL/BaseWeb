package com.example.personalwebsite.model.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BlogPostRequest {
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private String coverImage;
    private String status;
    private Boolean isTop;
    private Long authorId;
    private Long categoryId;
    private List<Long> tagIds;
    private LocalDateTime publishedAt;
}