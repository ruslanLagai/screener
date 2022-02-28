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
public interface DailyEmaRepository extends JpaRepository<DailyEma, Long> {

    @Query(value = "insert into daily_ema(datetime, emaType, emaValue, daily_indicator_id)" +
            " values (:dateTime, :emaType, :ema, :indicatorId)", nativeQuery = true)
    @Modifying
    void insertEmaData(@Param("emaType") String emaType, @Param("ema") double ema,
                       @Param("date") LocalDateTime dateTime, @Param("indicatorId") long indicatorId);
}
