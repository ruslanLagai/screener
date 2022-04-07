package com.home.project.stocks.repository;

import com.home.project.stocks.model.entity.WeeklyLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyLevelsRepository extends JpaRepository<WeeklyLevel, Long> {
    WeeklyLevel findByTicker(String ticker);

}
