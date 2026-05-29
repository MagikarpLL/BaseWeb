package com.example.personalwebsite.model.dto.response;

import lombok.Data;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Integer sort;
}