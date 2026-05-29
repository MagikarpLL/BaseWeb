package com.example.personalwebsite.service.impl;

import com.example.personalwebsite.exception.BadRequestException;
import com.example.personalwebsite.exception.ResourceNotFoundException;
import com.example.personalwebsite.mapper.PageViewMapper;
import com.example.personalwebsite.model.entity.PageView;
import com.example.personalwebsite.service.PageViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PageViewServiceImpl implements PageViewService {

    private final PageViewMapper pageViewMapper;

    public PageViewServiceImpl(PageViewMapper pageViewMapper) {
        this.pageViewMapper = pageViewMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageView findById(Long id) {
        PageView pageView = pageViewMapper.findById(id);
        if (pageView == null) {
            throw new ResourceNotFoundException("PageView", "id", id);
        }
        return pageView;
    }

    @Override
    @Transactional(readOnly = true)
    public PageView findByPageUrlAndDateAndHour(String pageUrl, LocalDate date, Integer hour) {
        return pageViewMapper.findByPageUrlAndDateAndHour(pageUrl, date, hour);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageView> findByDate(LocalDate date) {
        return pageViewMapper.findByDate(date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageView> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return pageViewMapper.findByDateRange(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer sumPvByDate(LocalDate date) {
        return pageViewMapper.sumPvByDate(date);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer sumUvByDate(LocalDate date) {
        return pageViewMapper.sumUvByDate(date);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByDate(LocalDate date) {
        Integer pv = pageViewMapper.sumPvByDate(date);
        return pv != null ? pv : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageView> findTopPages(LocalDate startDate, LocalDate endDate, Integer limit) {
        return pageViewMapper.findTopPages(startDate, endDate, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageView> findTopSources(LocalDate startDate, LocalDate endDate, Integer limit) {
        return pageViewMapper.findTopSources(startDate, endDate, limit);
    }

    @Override
    @Transactional
    public PageView createOrUpdate(PageView pageView) {
        if (pageView.getPageUrl() == null || pageView.getPageUrl().trim().isEmpty()) {
            throw new BadRequestException("Page URL cannot be empty");
        }
        if (pageView.getDate() == null) {
            throw new BadRequestException("Date cannot be null");
        }
        if (pageView.getHour() == null) {
            throw new BadRequestException("Hour cannot be null");
        }

        PageView existing = pageViewMapper.findByPageUrlAndDateAndHour(
                pageView.getPageUrl(), pageView.getDate(), pageView.getHour());

        if (existing != null) {
            existing.setPvCount(existing.getPvCount() + (pageView.getPvCount() != null ? pageView.getPvCount() : 1));
            existing.setUvCount(existing.getUvCount() + (pageView.getUvCount() != null ? pageView.getUvCount() : 0));
            if (pageView.getReferrer() != null) {
                existing.setReferrer(pageView.getReferrer());
            }
            pageViewMapper.update(existing);
            log.debug("Updated page view for {} on {}:{}", pageView.getPageUrl(), pageView.getDate(), pageView.getHour());
            return existing;
        } else {
            if (pageView.getPvCount() == null) {
                pageView.setPvCount(1);
            }
            if (pageView.getUvCount() == null) {
                pageView.setUvCount(0);
            }
            pageView.setCreatedAt(LocalDateTime.now());
            pageViewMapper.insert(pageView);
            log.debug("Created page view for {} on {}:{}", pageView.getPageUrl(), pageView.getDate(), pageView.getHour());
            return pageView;
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        PageView pageView = findById(id);
        pageViewMapper.deleteById(id);
        log.info("Deleted page view: {} (id: {})", pageView.getPageUrl(), id);
    }
}