package com.home.project.stocks.repository;

import com.home.project.stocks.model.entity.DailyEma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

/**
 * @author rlagay
 */
public interface DailyRsiRepository extends JpaRepository<DailyEma, Long> {

    @Query(value = "insert into daily_rsi(datetime, rsiValue, daily_indicator_id)" +
            " values (:dateTime, :rsi, :indicatorId)", nativeQuery = true)
    @Modifying
    void insertEmaData(@Param("rsi") double rsi, @Param("date") LocalDateTime dateTime,
                       @Param("indicatorId") long indicatorId);
}
