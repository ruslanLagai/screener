package com.home.project.stocks.service.impl;

import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.processor.LevelProcessor;
import com.home.project.stocks.service.CandlesService;
import com.home.project.stocks.service.DbUpdateService;
import com.home.project.stocks.service.WeeklyScanService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service find support/resistance levels on weekly candles
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WeeklyLevelScanService implements WeeklyScanService {

    @Value("${screener.level.candle-number}")
    private int period;

    private final CandlesService candlesService;
    private final List<LevelProcessor> levelProcessors;
    private final DbUpdateService dbUpdateService;

    @Override
    public void processStock(@NonNull String ticker) {
        Set<Double> levels = Collections.synchronizedSet(new HashSet<>());
        var candles= candlesService.getHistoricalCandles(ticker, Interval.TWELVE_DATA_ONE_WEEK, period);
        levelProcessors.forEach(levelProcessor -> {
            var result = levelProcessor.processStock(ticker, candles);
            levels.addAll(result);
        });
        log.info("Finished levels detecting for {}, number of levels {}", ticker, levels.size());
        dbUpdateService.saveWeeklyLevels(ticker, levels);
    }
}
