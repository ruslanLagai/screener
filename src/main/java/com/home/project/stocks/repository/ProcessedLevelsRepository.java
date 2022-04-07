package com.home.project.stocks.repository;

import com.home.project.stocks.model.entity.ProcessedLevels;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Set;

public interface ProcessedLevelsRepository extends JpaRepository<ProcessedLevels, Long> {
    ProcessedLevels findByTicker(String ticker);
    Set<ProcessedLevels> findByDateAfter(LocalDateTime date);
}
