package com.home.project.stocks.service.impl;

import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.entity.Levels;
import com.home.project.stocks.model.entity.ProcessedLevels;
import com.home.project.stocks.model.entity.WeeklyLevel;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.repository.WeeklyLevelsRepository;
import com.home.project.stocks.service.CandlesService;
import com.home.project.stocks.service.DailyScanService;
import com.home.project.stocks.service.DbUpdateService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service find support/resistance levels on weekly candles
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DailyLevelScanService implements DailyScanService {

    private final CandlesService dailyCandlesService;
    private final WeeklyLevelsRepository weeklyLevelsRepository;
    private final DbUpdateService dbUpdateService;

    @Override
    public void processStock(@NonNull String ticker, String figi) {
        var candles= dailyCandlesService.getHistoricalCandles(ticker, Interval.TWELVE_DATA_ONE_DAY, 35);
        if (CollectionUtils.isEmpty(candles) || candles.size() < 25) {
            log.warn("Not enough candles to detect levels, ticker {}", ticker);
            return;
        }
        var levels = Optional.ofNullable(weeklyLevelsRepository.findByTicker(ticker))
                .map(WeeklyLevel::getLevels)
                .orElse(Collections.emptySet());
        if (CollectionUtils.isEmpty(levels)) {
            log.warn("No levels found for {}", ticker);
            return;
        }
        candles.stream().max(Comparator.comparing(Candle::getDatetime))
                .ifPresent(candle -> {
                    var closestLevel = levels.stream()
                            .min(Comparator.comparing(level -> Math.abs(level.getValue() - candle.getC())))
                            .orElse(Levels.builder().build());
                    var price = candle.getC() > closestLevel.getValue() ? candle.getL() : candle.getH();
                    if (Math.abs(closestLevel.getValue() - price) / price < 0.03
                            && isNotCloseRetest(candles, closestLevel.getValue(), price)) {
                        dbUpdateService.saveProcessedLevels(ProcessedLevels.builder()
                                .closePrice(Precision.round(candle.getC(), 2))
                                .level(Precision.round(closestLevel.getValue(), 2))
                                .ticker(ticker)
                                .levelType(price > closestLevel.getValue() ? ProcessingResult.LevelType.SUPPORT
                                        : ProcessingResult.LevelType.RESISTANCE)
                                .date(LocalDateTime.now())
                                .build());
                    }
                });
    }

    private boolean isNotCloseRetest(List<Candle> candles, double level, double price) {
        return candles.stream()
                .map(candle -> price > level ? candle.getL() : candle.getH())
                .allMatch(value -> price > level ? value > level : value < level);
    }
}
