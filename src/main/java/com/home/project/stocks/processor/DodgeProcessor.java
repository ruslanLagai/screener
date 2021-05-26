package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.Trend;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.stream.Stream;

/**
 * Class to process dodge pattern.
 * MIN_INTERVAL, MAX_INTERVAL can be adjusted to check candle body
 */
@Component
@Log4j2
public class DodgeProcessor implements StocksProcessor {

    private static final double MIN_INTERVAL = 0.97;
    private static final double MAX_INTERVAL = 1.03;

    @Override
    public MultiValueMap<Processors, Candle> processStock(String figi, String ticker, Candle[] candles) {
        //stores candle index &&
        log.info("Processing stock, figi: " + figi);
        MultiValueMap<Processors, Candle> dodges = new LinkedMultiValueMap<>();
        Stream.of(candles)
            .filter(DodgeProcessor::checkDifference)
            .map(candle -> Arrays.asList(candles).indexOf(candle))
            .forEach(index -> {
                if (isDodge(candles, index)) {
                    dodges.addIfAbsent(Processors.DODGE, candles[index]);
                }
            });
        return dodges;
    }

    private boolean isDodge(Candle[] candles, Integer index) {
        if (index > candles.length - 2) {
            log.info("Skipping last two candles");
            return false;
        }
        var prevTrend = checkTrend(candles[index - 1], candles[index - 2]);
        var followingTrend = checkTrend(candles[index + 1], candles[index + 2]);
        var isClearTrend = followingTrend != null && prevTrend != followingTrend;
        var hasShadow = checkShadow(candles[index]);
        return isClearTrend && hasShadow;
    }

    private static Trend checkTrend(Candle first, Candle second) {
        Trend result = null;
        if (first.getC() > first.getO() && second.getC() > second.getO()) {
            result = Trend.ASCENDING;
        } else if (first.getC() < first.getO() && second.getC() < second.getO()) {
            result = Trend.DESCENDING;
        }
        return result;
    }

    private static boolean checkShadow(Candle candle) {
        var up = candle.getH() / candle.getC() > 1.1;
        var down = candle.getL() / candle.getC() < 0.9;
        return up && down;
    }

    private static boolean checkDifference(Candle candle) {
        var range = Range.between(MIN_INTERVAL, MAX_INTERVAL);
        return range.contains(candle.getO() / candle.getC());
    }
}
