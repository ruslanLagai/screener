package com.home.project.stocks.service.impl;

import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.entity.ProcessedLevels;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.service.CandlesService;
import com.home.project.stocks.service.LevelStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * @author rlagay
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DailyLevelStatisticServiceImpl implements LevelStatisticService {

    private static final int NUMBER_OF_CANDLES = 5;
    private static final int CLOSE_RETEST_DAYS = 30;
    private final CandlesService candlesService;


    @Value("${indicator.level.statistics.candles}")
    private int candleNumber;

    private final Map<ProcessingResult.LevelType, BiFunction<List<Candle>, ProcessedLevels, Map<LocalDateTime, List<Candle>>>> levelCrossing = Map.of(
        ProcessingResult.LevelType.SUPPORT, (candles, level) -> getInterval(candles, support(level)),
        ProcessingResult.LevelType.RESISTANCE, (candles, level) -> getInterval(candles, resistance(level))
    );

    private final Map<ProcessingResult.LevelType, BiConsumer<Map<LocalDateTime, List<Candle>>, ProcessedLevels>> levelStatistic = Map.of(
        ProcessingResult.LevelType.SUPPORT, this::processSupport,
        ProcessingResult.LevelType.RESISTANCE, this::processResistance
    );

    @Override
    public void analyzeStock(ProcessedLevels level, Interval interval) {
        try {
            var historicalCandles = candlesService.getHistoricalCandles(level.getTicker(), interval, candleNumber);
            if (CollectionUtils.isEmpty(historicalCandles)) {
                log.warn("No historical candles to analyze level, ticker {}", level.getTicker());
                return;
            }

            var sorted = historicalCandles.stream()
                .filter(candle -> level.getLevelType().equals(ProcessingResult.LevelType.SUPPORT)
                    ? supportFilter(level).test(candle) : resistanceFilter(level).test(candle))
                .sorted(Comparator.comparing(Candle::getDatetime))
                .toList();
            // find all level crossings
            var levelCrossings = historicalCandles.stream()
                .filter(candle -> candle.getL() <= level.getLevel() && candle.getH() >= level.getLevel())
                .map(Candle::getDatetime)
                .toList();

            var intervals = levelCrossing.get(level.getLevelType()).apply(sorted, level);
            // remove empty list && close retests
            List<LocalDateTime> toRemove = new ArrayList<>();
            intervals.forEach(((dateTime, candles) -> {
                if (candles.isEmpty()) {
                    toRemove.add(dateTime);
                }
                levelCrossings.stream()
                    .filter(dateTime1 -> dateTime1.isBefore(dateTime))
                    .filter(dateTime1 -> dateTime1 != dateTime)
                    .filter(dateTime1 -> Duration.between(dateTime1, dateTime).abs().toDays() < CLOSE_RETEST_DAYS)
                    .min(getClosestDateComparator(dateTime))
                    .stream().findFirst()
                    .ifPresent(dateTime1 -> toRemove.add(dateTime));
            }));
            toRemove.forEach(intervals::remove);

            levelStatistic.get(level.getLevelType()).accept(intervals, level);
        } catch (Exception e) {
            log.error("Failed to analyze level statistic, ticker {}", level.getTicker(), e);
        }
    }

    private static Comparator<LocalDateTime> getClosestDateComparator(LocalDateTime dateTime) {
        return (o1, o2) -> {
            long modul1 = Math.abs(Duration.between(dateTime, o1).toDays());
            long modul2 = Math.abs(Duration.between(dateTime, o2).toDays());
            return Long.compare(modul1, modul2);
        };
    }

    /**
     * Collect statistics for support level
     * @param intervals - map of interval, testing the level
     * @param level - level
     */
    private void processSupport(Map<LocalDateTime, List<Candle>> intervals, ProcessedLevels level) {
        //support
        AtomicInteger goodSignals = new AtomicInteger();
        List<Double> maxPrices = new ArrayList<>();
        List<Double> minPrices = new ArrayList<>();

        for (Map.Entry<LocalDateTime, List<Candle>> entry : intervals.entrySet()) {
            var candles = entry.getValue();

            candles.stream()
                .min(Comparator.comparing(Candle::getL))
                .map(Candle::getL)
                .ifPresent(minPrices::add);
            candles.stream()
                .max(Comparator.comparing(Candle::getH))
                .map(Candle::getH)
                .stream()
                .peek(maxPrices::add)
                .findFirst()
                .ifPresent(max -> {
                    if (max > level.getLevel() * 1.02) {
                        goodSignals.getAndIncrement();
                    }
                });
        }
        if (!intervals.isEmpty()) {
            setSupportStatistics(intervals, level, goodSignals, maxPrices, minPrices);
        }
    }

    /**
     * Collect statistics for resistance level
     * @param intervals - map of interval, testing the level
     * @param level - level
     */
    private void processResistance(Map<LocalDateTime, List<Candle>> intervals, ProcessedLevels level) {
        //support
        AtomicInteger goodSignals = new AtomicInteger();
        List<Double> maxPrices = new ArrayList<>();
        List<Double> minPrices = new ArrayList<>();

        for (Map.Entry<LocalDateTime, List<Candle>> entry : intervals.entrySet()) {
            var candles = entry.getValue();
            candles.stream()
                .min(Comparator.comparing(Candle::getL))
                .map(Candle::getL)
                .stream()
                .peek(minPrices::add)
                .findFirst()
                .ifPresent(min -> {
                    if (min < level.getLevel() * 0.98) {
                        goodSignals.getAndIncrement();
                    }
                });
            candles.stream()
                .max(Comparator.comparing(Candle::getH))
                .map(Candle::getH)
                .ifPresent(maxPrices::add);
        }
        if (!intervals.isEmpty()) {
            setResistanceStatistics(intervals, level, goodSignals, maxPrices, minPrices);
        }
    }

    private void setSupportStatistics(Map<LocalDateTime, List<Candle>> intervals, ProcessedLevels level, AtomicInteger goodSignals,
                                      List<Double> maxPrices, List<Double> minPrices) {
        double winPercentage = Integer.valueOf(intervals.size()).doubleValue() / Integer.valueOf(goodSignals.get()).doubleValue();
        double averageBreaking = level.getLevel() / minPrices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double averageRebound =  maxPrices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0) / level.getLevel();

        setStatistics(intervals, level, goodSignals, winPercentage, averageBreaking, averageRebound);
    }


    private void setResistanceStatistics(Map<LocalDateTime, List<Candle>> intervals, ProcessedLevels level, AtomicInteger goodSignals,
                                         List<Double> maxPrices, List<Double> minPrices) {
        double winPercentage = Integer.valueOf(intervals.size()).doubleValue() / Integer.valueOf(goodSignals.get()).doubleValue();
        double averageBreaking =  maxPrices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0) / level.getLevel();
        double averageRebound = level.getLevel() / minPrices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        setStatistics(intervals, level, goodSignals, winPercentage, averageBreaking, averageRebound);
    }

    private void setStatistics(Map<LocalDateTime, List<Candle>> intervals, ProcessedLevels level, AtomicInteger goodSignals,
                               double winPercentage, double averageBreaking, double averageRebound) {
        level.setSuccessRate(winPercentage);
        level.setAverageBreaking(Math.abs(Precision.round(1 - averageBreaking, 2)));
        level.setAverageRebound(Math.abs(Precision.round(1 - averageRebound, 2)));
        level.setTotalCrosses(intervals.size());
        level.setGoodSignals(goodSignals.get());
    }

    private Predicate<Candle> support(ProcessedLevels level) {
        return candle -> candle.getL() <= level.getLevel() && (candle.getO() + candle.getC()) / 2 > level.getLevel() && candle.getO() > level.getLevel();
    }

    private Predicate<Candle> resistance(ProcessedLevels level) {
        return candle -> candle.getH() >= level.getLevel() && (candle.getO() + candle.getC()) / 2 < level.getLevel() && candle.getO() < level.getLevel();
    }

    /**
     * Split candles to intervals near the level
     * - get candle crossed the level
     * - collect next NUMBER_OF_CANDLES candles to collect statistics
     * - ignoring close retest (< CLOSE_RETEST_DAYS days)
     *
     * @param candles sorted list of candles near the level
     * @param predicate filter
     * @return candles split to intervals
     */
    private Map<LocalDateTime, List<Candle>> getInterval(List<Candle> candles, Predicate<Candle> predicate) {
        LocalDateTime dateTime = null;
        List<Candle> interval = new ArrayList<>();
        Map<LocalDateTime, List<Candle>> intervalsByDate = new HashMap<>();
        boolean isNextInterval = false;
        for (Candle candle : candles) {
            if (predicate.test(candle) && (dateTime == null || candle.getDatetime().isAfter(dateTime.plusDays(CLOSE_RETEST_DAYS)))) {
                dateTime = candle.getDatetime();
                isNextInterval = true;
                continue;
            }
            if (dateTime != null && candle.getDatetime().isBefore(dateTime.plusDays(NUMBER_OF_CANDLES))) {
                interval.add(candle);
                continue;
            }
            if (dateTime != null && isNextInterval && (candle.getDatetime().isAfter(dateTime.plusDays(NUMBER_OF_CANDLES))
                    || candle.getDatetime().isEqual(dateTime.plusDays(NUMBER_OF_CANDLES)))) {
                intervalsByDate.put(dateTime, new ArrayList<>(interval));
                interval.clear();
                isNextInterval = false;
                continue;
            }
            if (dateTime != null && !interval.isEmpty()) {
                intervalsByDate.put(dateTime, new ArrayList<>(interval));
            }
        }
        return intervalsByDate;
    }

    private Predicate<Candle> supportFilter(ProcessedLevels level) {
        return candle -> candle.getL() / level.getLevel() < 1.04 && candle.getL() / level.getLevel() > 0.97;
    }

    private Predicate<Candle> resistanceFilter(ProcessedLevels level) {
        return candle -> level.getLevel() / candle.getH() < 1.04 && level.getLevel() / candle.getH() > 0.97;
    }
}
