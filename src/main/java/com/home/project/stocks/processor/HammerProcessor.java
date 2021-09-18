package com.home.project.stocks.processor;

import com.home.project.stocks.model.aplha.vantage.Candle;
import com.home.project.stocks.model.processing.Trend;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to process Hammer pattern
 */
@Component
@Log4j2
public class HammerProcessor implements PatternProcessor {

    private static final double LOWER_SHADOW_RATIO = 1.1;
    private static final double LOWER_CANDLE_BODY_RATIO = 0.6;
    private static final double UPPER_CANDLE_BODY_RATIO = 0.95;

    @Override
    public MultiValueMap<Processors, Candle> processStock(String figi, String ticker,
                                                          Map<Date, Candle> candles) {
        log.info("Processing stock, ticker: " + ticker);
        MultiValueMap<Processors, Candle> hammers = new LinkedMultiValueMap<>();
        if (candles == null || candles.size() < 5) {
            log.warn(String.format("Not enough candles, ticker %s", ticker));
            return hammers;
        }
        var sorted = candles.keySet().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        var dateToProcess = sorted.get(sorted.size() - 2);
        Optional.ofNullable(candles.get(dateToProcess))
                .filter(candle -> {
                    var isPrevDesc = isPrevDesc(candles, dateToProcess);
                    var isNextAsc = isNextAsc(candles, dateToProcess);
                    return isNextAsc && isPrevDesc;
                })
                .filter(HammerProcessor::hasBody)
                .filter(HammerProcessor::isUpperPart)
                .filter(HammerProcessor::hasShadow)
                .ifPresent(candle -> hammers.addIfAbsent(Processors.HAMMER, candle));
        return hammers;
    }

    /**
     * Check if candle has long lower shadow
     *
     * @param candle candle
     * @return hasShadow
     */
    private static boolean hasShadow(Candle candle) {
        return candle.getOpen() / candle.getLow() > LOWER_SHADOW_RATIO;
    }

    /**
     * Check if candle has body
     *
     * @param candle candle to process
     * @return hasBody
     */
    private static boolean hasBody(Candle candle) {
        var body = Math.abs(candle.getOpen() / candle.getClose());
        var range = Range.between(LOWER_CANDLE_BODY_RATIO, UPPER_CANDLE_BODY_RATIO);
        return range.contains(body);
    }

    /**
     * Check if candle candle has long lower shadow
     *
     * @param candle candle to process
     * @return isUpperPart
     */
    private static boolean isUpperPart(Candle candle) {
        var middle = (candle.getHigh() - candle.getLow()) / 2 + candle.getLow();
        return candle.getOpen() > middle;
    }

    private static boolean isPrevDesc(Map<Date, Candle> candles, Date dateToProcess) {
        var sorted = candles.keySet().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        var processIndex = sorted.indexOf(dateToProcess);
        if (candles.size() < 5 || processIndex < 3) {
            log.error("Index out of bound");
            return false;
        }
        var trend = extractTrend(
                candles.get(sorted.get(processIndex - 3)),
                candles.get(sorted.get(processIndex - 2)),
                candles.get(sorted.get(processIndex - 1))
        );
        return trend != null && trend != Trend.ASCENDING;
    }

    private static boolean isNextAsc(Map<Date, Candle> candles, Date dateToProcess) {
        if (candles.size() < 5) {
            return false;
        }
        var sorted = candles.keySet().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        var processIndex = sorted.indexOf(dateToProcess);
        if (candles.size() < 5 || processIndex > candles.size() - 2) {
            log.error("Index out of bound");
            return false;
        }
        var candle = candles.get(sorted.get(processIndex + 1));
        return candle.getClose() - candle.getOpen() > 0;
    }

    private static Trend extractTrend(Candle first, Candle second, Candle third) {
        Trend result = null;
        if (first.getClose() > first.getOpen() && second.getClose() > second.getOpen()
                && third.getClose() > third.getOpen()) {
            result = Trend.ASCENDING;
        } else if (first.getClose() < first.getOpen() && second.getClose() < second.getOpen()
                && third.getClose() < third.getOpen()) {
            result = Trend.DESCENDING;
        }
        return result;
    }

}
