package com.example.personalwebsite.service.impl;

import com.example.personalwebsite.exception.BadRequestException;
import com.example.personalwebsite.exception.ResourceNotFoundException;
import com.example.personalwebsite.mapper.DailyStatsMapper;
import com.example.personalwebsite.model.entity.DailyStats;
import com.example.personalwebsite.service.DailyStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DailyStatsServiceImpl implements DailyStatsService {

    private final DailyStatsMapper dailyStatsMapper;

    public DailyStatsServiceImpl(DailyStatsMapper dailyStatsMapper) {
        this.dailyStatsMapper = dailyStatsMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public DailyStats findById(Long id) {
        DailyStats stats = dailyStatsMapper.findById(id);
        if (stats == null) {
            throw new ResourceNotFoundException("DailyStats", "id", id);
        }
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public DailyStats findByDate(LocalDate date) {
        return dailyStatsMapper.findByDate(date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyStats> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return dailyStatsMapper.findByDateRange(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyStats> findRecent(Integer limit) {
        return dailyStatsMapper.findRecent(limit);
    }

    @Override
    @Transactional
    public DailyStats upsert(DailyStats dailyStats) {
        if (dailyStats.getDate() == null) {
            throw new BadRequestException("Date cannot be null");
        }
        if (dailyStats.getTotalPv() == null) {
            dailyStats.setTotalPv(0);
        }
        if (dailyStats.getTotalUv() == null) {
            dailyStats.setTotalUv(0);
        }
        if (dailyStats.getCreatedAt() == null) {
            dailyStats.setCreatedAt(LocalDateTime.now());
        }
        dailyStatsMapper.upsert(dailyStats);
        log.debug("Upserted daily stats for {}", dailyStats.getDate());
        return dailyStats;
    }

    @Override
    @Transactional
    public DailyStats createOrUpdate(DailyStats dailyStats) {
        if (dailyStats.getDate() == null) {
            throw new BadRequestException("Date cannot be null");
        }

        DailyStats existing = dailyStatsMapper.findByDate(dailyStats.getDate());

        if (existing != null) {
            existing.setTotalPv(dailyStats.getTotalPv() != null ? dailyStats.getTotalPv() : existing.getTotalPv());
            existing.setTotalUv(dailyStats.getTotalUv() != null ? dailyStats.getTotalUv() : existing.getTotalUv());
            dailyStatsMapper.update(existing);
            log.debug("Updated daily stats for {}", dailyStats.getDate());
            return existing;
        } else {
            if (dailyStats.getTotalPv() == null) {
                dailyStats.setTotalPv(0);
            }
            if (dailyStats.getTotalUv() == null) {
                dailyStats.setTotalUv(0);
            }
            dailyStats.setCreatedAt(LocalDateTime.now());
            dailyStatsMapper.insert(dailyStats);
            log.debug("Created daily stats for {}", dailyStats.getDate());
            return dailyStats;
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        DailyStats stats = findById(id);
        dailyStatsMapper.deleteById(id);
        log.info("Deleted daily stats: {} (id: {})", stats.getDate(), id);
    }
}