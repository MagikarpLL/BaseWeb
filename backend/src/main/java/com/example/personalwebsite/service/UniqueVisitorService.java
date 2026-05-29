package com.example.personalwebsite.service;

import com.example.personalwebsite.model.entity.UniqueVisitor;
import java.util.List;

public interface UniqueVisitorService {
    UniqueVisitor findById(Long id);
    UniqueVisitor findByVisitorId(String visitorId);
    List<UniqueVisitor> findAll();
    UniqueVisitor createOrUpdate(UniqueVisitor visitor);
    void deleteById(Long id);
    long countTotal();
    long countRecent(java.time.LocalDateTime since);
    long countByDate(java.time.LocalDate date);
}