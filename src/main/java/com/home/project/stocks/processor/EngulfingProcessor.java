package com.home.project.stocks.processor;

import java.util.*;
import java.util.stream.Collectors;

import com.home.project.stocks.model.candles.Candle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.home.project.stocks.model.processing.Trend;

/**
 * Class to process Engulfing pattern (бычье поглощение).
 * MIN_INTERVAL, MAX_INTERVAL can be adjusted to check candle body
 */
@Component
@Slf4j
public class EngulfingProcessor implements PatternProcessor {

    @Override
    public HashMap<Processors, Candle> processStock(String figi, String ticker, List<Candle> candles) {
        log.info("Processing stock, ticker: " + ticker);
        HashMap<Processors, Candle> patterns = new HashMap<>();

        if (candles == null || candles.size() < 4) {
            log.warn(String.format("Not enough candles, ticker %s", ticker));
            return patterns;
        }
        var sorted = candles.stream()
                .sorted(Comparator.comparing(Candle::getDatetime, Comparator.reverseOrder()))
                .collect(Collectors.toList());
        var candleToProcess = sorted.get(0);
        Optional.of(candleToProcess)
                .filter(candle -> isTrendDesc(sorted))
                .filter(EngulfingProcessor::checkShadow)
                .filter(candle -> checkCandleBodies(candle, sorted.get(1)))
                .filter(candle -> checkTrendChange(candle, sorted.get(1)))
                .ifPresent(candle -> {
                    log.info("Stock has pattern, ticker {}", ticker);
                    patterns.put(Processors.ENGULFING, candle);
                });
        return patterns;
    }

    /**
     * Check if there is pattern:
     * - hasShadow
     * - clear trend
     * - small body
     *
     * @param candles       sequence of candles
     * @return has trend
     */
    private boolean isTrendDesc(List<Candle> candles) {
        if (candles.size() < 4) {
            log.info("Not enough candles");
            return false;
        }

        var prevTrend = checkTrend(
                candles.get(1),
                candles.get(2),
                candles.get(3)
        );
        return prevTrend == Trend.DESCENDING;
    }

    /**
     * Check if candles have trend: ASC -> DESC / DESC -> ASC
     * 2 Candles are considered
     *
     * @param first      first candle
     * @param second     second candle
     * @return Trend
     */
    private static Trend checkTrend(Candle first, Candle second, Candle third) {
        Trend result = null;
        if (first.getC() > first.getO() && second.getC() > second.getO() && third.getC() > third.getO()) {
            result = Trend.ASCENDING;
        } else if (first.getC() < first.getO() && second.getC() < second.getO() && third.getC() < third.getO()) {
            result = Trend.DESCENDING;
        }
        return result;
    }

    /**
     * Check if candle has shadows: upper and lower
     *
     * @param candle     candle to process
     * @return           does candle have shadow
     */
    private static boolean checkShadow(Candle candle) {
        var up = candle.getH() / Double.max(candle.getC(), candle.getO()) < 1.05;
        var down = candle.getL() / Double.min(candle.getC(), candle.getO()) > 0.95;
        return up && down;
    }

    /**
     * Check that prev candle engulfs the latest
     * works ONLY for bull Engulfing
     *
     * @param lastCandle     candle to process
     * @param prevCandle     previous candle
     * @return does candle have small body
     */
    private static boolean checkCandleBodies(Candle lastCandle, Candle prevCandle) {
        return prevCandle.getO() < lastCandle.getC() && prevCandle.getC() >= lastCandle.getO();
    }

    /**
     * Check that prev candle is red && the latest is green
     * works ONLY for bull Engulfing
     *
     * @param lastCandle     candle to process
     * @param prevCandle     previous candle
     * @return does candle have small body
     */
    private static boolean checkTrendChange(Candle lastCandle, Candle prevCandle) {
        return prevCandle.getO() > prevCandle.getC() && lastCandle.getO() < lastCandle.getC();
    }
}
