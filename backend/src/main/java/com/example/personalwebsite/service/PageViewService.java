package com.example.personalwebsite.service;

import com.example.personalwebsite.model.entity.PageView;
import java.time.LocalDate;
import java.util.List;

public interface PageViewService {
    PageView findById(Long id);
    PageView findByPageUrlAndDateAndHour(String pageUrl, LocalDate date, Integer hour);
    List<PageView> findByDate(LocalDate date);
    List<PageView> findByDateRange(LocalDate startDate, LocalDate endDate);
    Integer sumPvByDate(LocalDate date);
    Integer sumUvByDate(LocalDate date);
    long countByDate(LocalDate date);
    List<PageView> findTopPages(LocalDate startDate, LocalDate endDate, Integer limit);
    List<PageView> findTopSources(LocalDate startDate, LocalDate endDate, Integer limit);
    PageView createOrUpdate(PageView pageView);
    void deleteById(Long id);
}