package com.example.personalwebsite.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendResponse {
    private LocalDate date;
    private Integer pv;
    private Integer uv;
}
