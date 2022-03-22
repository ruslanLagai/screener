package com.home.project.stocks.repository;

import com.home.project.stocks.model.entity.DailyIndicator;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface DailyIndicatorDataRepository extends JpaRepository<DailyIndicator, Long> {
    DailyIndicator getByTickerAndDateAfter(String ticker, LocalDateTime dateTimeFrom);
    DailyIndicator getByTickerAndDateAndTimeframe(String ticker, LocalDateTime date, String timeframe);

//    @Query(value = "select distinct daily_indicator from daily_indicator " +
//            "left join fetch daily_rsi on daily_rsi.daily_indicator_id = daily_indicator.id " +
//            "left join fetch daily_ema on daily_ema.daily_indicator_id = daily_indicator.id " +
//            "left join fetch daily_macd on daily_macd.daily_indicator_id = daily_indicator.id " +
//            "where daily_indicator.ticker = ?1 and date > ?2")


    @EntityGraph(value = "DailyIndicator.fetch-all-data-entity-graph")
    DailyIndicator findByTicker(@Param("ticker") String ticker);

}
