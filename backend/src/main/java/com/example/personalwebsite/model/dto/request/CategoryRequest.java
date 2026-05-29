package com.example.personalwebsite.model.dto.request;

import lombok.Data;

@Data
public class CategoryRequest {
    private String name;
    private String slug;
    private String description;
    private Integer sort;
}