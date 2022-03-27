package com.home.project.stocks.service.impl;

import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.processor.PatternProcessor;
import com.home.project.stocks.service.CandlesService;
import com.home.project.stocks.service.DailyScanService;
import com.home.project.stocks.service.DbUpdateService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.home.project.stocks.model.entity.Candle.populateFields;

/**
 * Service to save identified patterns to db
 */
@Component
@Slf4j
public class DailyPatternScanService implements DailyScanService {
    private final CandlesService dailyCandlesService;
    private final List<PatternProcessor> patternProcessors;
    private final DbUpdateService dbUpdateService;

    public DailyPatternScanService(CandlesService dailyCandlesService,
                                   List<PatternProcessor> patternProcessors,
                                   DbUpdateService dbUpdateService) {
        this.dailyCandlesService = dailyCandlesService;
        this.patternProcessors = patternProcessors;
        this.dbUpdateService = dbUpdateService;
    }

    @Override
    public void processStock(@NonNull String ticker, String figi) {
        Map<PatternProcessor.Processors, Candle> result = new HashMap<>();
        var candles = dailyCandlesService.getCandles(ticker, Interval.TWELVE_DATA_ONE_DAY);
        if (CollectionUtils.isEmpty(candles) || candles.size() < 4) {
            log.warn("Not enough candles to detect pattern, ticker {}, number of candles {}", ticker,
                    candles != null ? candles.size() : null);
            return;
        }
        candles.forEach(candle -> candle.setInterval(Interval.TWELVE_DATA_ONE_DAY.getInterval()));
        patternProcessors.forEach(patternProcessor ->
                result.putAll(patternProcessor.processStock(figi, ticker, candles)));
        if (!result.isEmpty()) {
            dbUpdateService.savePattern(populateFields(result.containsKey(PatternProcessor.Processors.HAMMER)
                    ? result.get(PatternProcessor.Processors.HAMMER)
                    : result.get(PatternProcessor.Processors.DODGE), ticker, figi,
                    result.containsKey(PatternProcessor.Processors.DODGE), result.containsKey(PatternProcessor.Processors.HAMMER)));
        }
    }
}
