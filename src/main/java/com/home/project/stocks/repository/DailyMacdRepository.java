package com.home.project.stocks.repository;

import com.home.project.stocks.model.entity.DailyMacd;
import com.home.project.stocks.model.entity.DailyRsi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

/**
 * @author rlagay
 */
public interface DailyMacdRepository extends JpaRepository<DailyMacd, Long> {

    @Query(value = "insert into daily_macd(datetime, macdHistValue, macdSignalValue, macdValue, daily_indicator_id)" +
            " values (:date, :macdHistValue, :macdSignalValue, :macdValue, :indicatorId)", nativeQuery = true)
    @Modifying
    @Transactional
    void insertMacdData(@Param("macdHistValue") double macdHistValue, @Param("macdSignalValue") double macdSignalValue,
                        @Param("macdValue") double macdValue, @Param("date") LocalDateTime dateTime,
                       @Param("indicatorId") long indicatorId);
}
