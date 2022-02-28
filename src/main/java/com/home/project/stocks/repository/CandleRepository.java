package com.home.project.stocks.repository;

import com.home.project.stocks.model.entity.Candle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CandleRepository extends JpaRepository<Candle, Long> {
    Candle findByTickerAndTimeAfter(String ticker, LocalDateTime dateTime);

    List<Candle> findByTimeAfter(LocalDateTime dateTime);
}
