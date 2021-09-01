package com.home.project.stocks.processor;

import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.home.project.stocks.model.aplha.vantage.Candle;
import com.home.project.stocks.model.processing.Trend;

import lombok.extern.log4j.Log4j2;
/**
 * Class to process dodge pattern.
 * MIN_INTERVAL, MAX_INTERVAL can be adjusted to check candle body
 */
@Component
@Log4j2
public class DodgeProcessor implements PatternProcessor {

    private static final double MIN_INTERVAL = 0.97;
    private static final double MAX_INTERVAL = 1.03;

    @Override
    public MultiValueMap<Processors, Candle> processStock(String figi, String ticker,
                                                          Map<Date, Candle> candles) {
        log.info("Processing stock, ticker: " + ticker);
        MultiValueMap<Processors, Candle> dodges = new LinkedMultiValueMap<>();
        if (candles == null || candles.size() < 5) {
            log.warn(String.format("Not enough candles, ticker %s", ticker));
            return dodges;
        }
        var sorted = candles.keySet().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        var dateToProcess = sorted.get(sorted.size() - 3);
        Optional.ofNullable(candles.get(dateToProcess))
                .filter(DodgeProcessor::checkDifference)
                .ifPresent(candle -> {
                    if (isDodge(candles, dateToProcess)) {
                        log.info(String.format("Stock has dodge pattern, ticker %s", ticker));
                        dodges.addIfAbsent(Processors.DODGE, candle);
                    }
                });
        return dodges;
    }

    /**
     * Check if there is dodge pattern:
     *  - hasShadow
     *  - clear trend
     *  - small body
     * @param candles sequence of candles
     * @param dateToProcess which candle to check on dodge
     * @return isDodge
     */
    private boolean isDodge(Map<Date, Candle> candles, Date dateToProcess) {
        if (candles.size() < 5) {
            log.info("Not enough candles");
            return false;
        }
        var sorted = candles.keySet().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        var processIndex = sorted.indexOf(dateToProcess);
        if (processIndex < 2 || processIndex > candles.size() - 3) {
            log.error("Index out of bound");
        }

        var prevTrend = checkTrend(
                candles.get(sorted.get(processIndex - 1)),
                candles.get(sorted.get(processIndex - 2))
        );
        var followingTrend = checkTrend(
                candles.get(sorted.get(processIndex + 1)),
                candles.get(sorted.get(processIndex + 2))
        );
        var isClearTrend = followingTrend != null && prevTrend != followingTrend;
        var hasShadow = checkShadow(candles.get(dateToProcess));
        return isClearTrend && hasShadow;
    }

    /**
     * Check if candles have trend: ASC -> DESC / DESC -> ASC
     * 2 Candles are considered
     * @param first - first candle
     * @param second - second candle
     * @return Trend
     */
    private static Trend checkTrend(Candle first, Candle second) {
        Trend result = null;
        if (first.getClose() > first.getOpen() && second.getClose() > second.getOpen()) {
            result = Trend.ASCENDING;
        } else if (first.getClose() < first.getOpen() && second.getClose() < second.getOpen()) {
            result = Trend.DESCENDING;
        }
        return result;
    }

    /**
     * Check if candle has shadows: upper and lower
     * @param candle - candle to process
     * @return does candle have shadow
     */
    private static boolean checkShadow(Candle candle) {
        var up = candle.getHigh() / candle.getClose() > 1.1;
        var down = candle.getLow() / candle.getClose() < 0.9;
        return up && down;
    }

    /**
     * Check that candle has small body
     * @param candle - candle to process
     * @return does candle have small body
     */
    private static boolean checkDifference(Candle candle) {
        var range = Range.between(MIN_INTERVAL, MAX_INTERVAL);
        return range.contains(candle.getOpen() / candle.getClose());
    }
}
