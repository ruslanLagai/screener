package com.home.project.stocks.service;

import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.model.entity.ProcessedLevels;

/**
 * @author rlagay
 */
public interface LevelStatisticService {

    void analyzeStock(ProcessedLevels level, Interval interval);
}
