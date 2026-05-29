package com.example.personalwebsite.service;

import com.example.personalwebsite.model.entity.DailyStats;
import java.time.LocalDate;
import java.util.List;

public interface DailyStatsService {
    DailyStats findById(Long id);
    DailyStats findByDate(LocalDate date);
    List<DailyStats> findByDateRange(LocalDate startDate, LocalDate endDate);
    List<DailyStats> findRecent(Integer limit);
    DailyStats upsert(DailyStats dailyStats);
    DailyStats createOrUpdate(DailyStats dailyStats);
    void deleteById(Long id);
}