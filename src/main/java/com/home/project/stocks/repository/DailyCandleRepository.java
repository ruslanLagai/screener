package com.home.project.stocks.repository;

import com.home.project.stocks.model.entity.DailyCandle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyCandleRepository extends JpaRepository<DailyCandle, Long> {
    List<DailyCandle> findByTicker(String ticker);
}
