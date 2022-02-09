package com.home.project.stocks.processor;

import java.util.*;
import java.util.stream.Collectors;

import com.home.project.stocks.model.candles.Candle;
import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import com.home.project.stocks.model.processing.Trend;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.MultiValueMap;

/**
 * Class to process dodge pattern.
 * MIN_INTERVAL, MAX_INTERVAL can be adjusted to check candle body
 */
@Component
@Log4j2
public class DodgeProcessor implements PatternProcessor {

    private static final double MIN_INTERVAL = 0.97;
    private static final double MAX_INTERVAL = 1.03;
    private static final Range<Double> range = Range.between(MIN_INTERVAL, MAX_INTERVAL);

    @Override
    public HashMap<Processors, Candle> processStock(String figi, String ticker,
                                                          List<Candle> candles) {
        log.info("Processing stock, ticker: " + ticker);
        HashMap<Processors, Candle> dodges = new HashMap<>();
        if (candles == null || candles.size() < 4) {
            log.warn(String.format("Not enough candles, ticker %s", ticker));
            return dodges;
        }
        var sorted = candles.stream().sorted(Comparator.comparing(Candle::getTime)).collect(Collectors.toList());
        var candleToProcess = sorted.get(sorted.size() - 1);
        Optional.of(candleToProcess)
                .filter(DodgeProcessor::checkDifference)
                .ifPresent(candle -> {
                    if (isDodge(sorted)) {
                        log.info(String.format("Stock has dodge pattern, ticker %s", ticker));
                        dodges.put(Processors.DODGE, candle);
                    }
                });
        return dodges;
    }

    /**
     * Check if there is dodge pattern:
     * - hasShadow
     * - clear trend
     * - small body
     *
     * @param candles       sequence of candles
     * @return isDodge
     */
    private boolean isDodge(List<Candle> candles) {
        if (candles.size() < 3) {
            log.info("Not enough candles");
            return false;
        }

        var prevTrend = checkTrend(
                candles.get(candles.size() - 2),
                candles.get(candles.size() - 3)
        );
        var isClearTrend = prevTrend != null;
        var hasShadow = checkShadow(candles.get(candles.size() - 1));
        return isClearTrend && hasShadow;
    }

    /**
     * Check if candles have trend: ASC -> DESC / DESC -> ASC
     * 2 Candles are considered
     *
     * @param first  - first candle
     * @param second - second candle
     * @return Trend
     */
    private static Trend checkTrend(Candle first, Candle second) {
        Trend result = null;
        if (first.getC() > first.getO() && second.getC() > second.getO()) {
            result = Trend.ASCENDING;
        } else if (first.getC() < first.getO() && second.getC() < second.getO()) {
            result = Trend.DESCENDING;
        }
        return result;
    }

    /**
     * Check if candle has shadows: upper and lower
     *
     * @param candle - candle to process
     * @return does candle have shadow
     */
    private static boolean checkShadow(Candle candle) {
        var up = candle.getH() / Double.max(candle.getC(), candle.getO()) > 1.03;
        var down = candle.getL() / Double.min(candle.getC(), candle.getO()) < 0.97;
        return up && down;
    }

    /**
     * Check that candle has small body
     *
     * @param candle - candle to process
     * @return does candle have small body
     */
    private static boolean checkDifference(Candle candle) {
        return range.contains(candle.getO() / candle.getC());
    }
}
