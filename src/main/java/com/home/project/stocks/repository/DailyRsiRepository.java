package com.home.project.stocks.repository;

import com.home.project.stocks.model.entity.DailyRsi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

/**
 * @author rlagay
 */
public interface DailyRsiRepository extends JpaRepository<DailyRsi, Long> {

    @Query(value = "insert into daily_rsi(datetime, rsiValue, daily_indicator_id)" +
            " values (:date, :rsi, :indicatorId)", nativeQuery = true)
    @Modifying
    void insertRsiData(@Param("rsi") double rsi, @Param("date") LocalDateTime dateTime,
                       @Param("indicatorId") long indicatorId);
}
