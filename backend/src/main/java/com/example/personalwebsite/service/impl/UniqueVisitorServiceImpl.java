package com.example.personalwebsite.service.impl;

import com.example.personalwebsite.exception.BadRequestException;
import com.example.personalwebsite.exception.ResourceNotFoundException;
import com.example.personalwebsite.mapper.UniqueVisitorMapper;
import com.example.personalwebsite.model.entity.UniqueVisitor;
import com.example.personalwebsite.service.UniqueVisitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UniqueVisitorServiceImpl implements UniqueVisitorService {

    private final UniqueVisitorMapper uniqueVisitorMapper;

    public UniqueVisitorServiceImpl(UniqueVisitorMapper uniqueVisitorMapper) {
        this.uniqueVisitorMapper = uniqueVisitorMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UniqueVisitor findById(Long id) {
        UniqueVisitor visitor = uniqueVisitorMapper.findById(id);
        if (visitor == null) {
            throw new ResourceNotFoundException("UniqueVisitor", "id", id);
        }
        return visitor;
    }

    @Override
    @Transactional(readOnly = true)
    public UniqueVisitor findByVisitorId(String visitorId) {
        return uniqueVisitorMapper.findByVisitorId(visitorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UniqueVisitor> findAll() {
        return uniqueVisitorMapper.findAll();
    }

    @Override
    @Transactional
    public UniqueVisitor createOrUpdate(UniqueVisitor visitor) {
        if (visitor.getVisitorId() == null || visitor.getVisitorId().trim().isEmpty()) {
            throw new BadRequestException("Visitor ID cannot be empty");
        }

        UniqueVisitor existing = uniqueVisitorMapper.findByVisitorId(visitor.getVisitorId());

        if (existing != null) {
            existing.setLastVisitAt(LocalDateTime.now());
            existing.setTotalPv(existing.getTotalPv() + (visitor.getTotalPv() != null ? visitor.getTotalPv() : 1));
            uniqueVisitorMapper.update(existing);
            log.debug("Updated visitor: {} (total pv: {})", visitor.getVisitorId(), existing.getTotalPv());
            return existing;
        } else {
            LocalDateTime now = LocalDateTime.now();
            visitor.setFirstVisitAt(now);
            visitor.setLastVisitAt(now);
            if (visitor.getTotalPv() == null) {
                visitor.setTotalPv(1);
            }
            uniqueVisitorMapper.insert(visitor);
            log.debug("Created new visitor: {} (visitor id: {})", visitor.getVisitorId(), visitor.getId());
            return visitor;
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        UniqueVisitor visitor = findById(id);
        uniqueVisitorMapper.deleteById(id);
        log.info("Deleted unique visitor: {} (id: {})", visitor.getVisitorId(), id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotal() {
        return uniqueVisitorMapper.countTotal();
    }

    @Override
    @Transactional(readOnly = true)
    public long countRecent(java.time.LocalDateTime since) {
        return uniqueVisitorMapper.countRecent(since);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByDate(java.time.LocalDate date) {
        return uniqueVisitorMapper.countByDate(date);
    }
}