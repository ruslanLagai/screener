package com.home.project.stocks.repository;

import com.home.project.stocks.model.entity.ProcessedIndicators;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndicatorRepository extends JpaRepository<ProcessedIndicators, String> {
    ProcessedIndicators getByTicker(String ticker);
}
