package com.home.project.stocks.repository;

import com.home.project.stocks.model.entity.ProcessedIndicators;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DailyProcessedIndicatorRepository extends JpaRepository<ProcessedIndicators, String> {
    ProcessedIndicators getByTicker(String ticker);
    List<ProcessedIndicators> getByDateAfter(LocalDateTime dateTime);
}
