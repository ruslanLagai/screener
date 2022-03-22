package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.Trend;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to process Hammer pattern
 */
@Component
@Log4j2
public class HammerProcessor implements PatternProcessor {

    private static final double UPPER_SHADOW_RATIO = 0.2;
    private static final double LOWER_SHADOW_RATIO = 1.3;
    private static final double LOWER_CANDLE_BODY_RATIO = 1;
    private static final double UPPER_CANDLE_BODY_RATIO = 10;

    @Override
    public Map<Processors, Candle> processStock(String figi, String ticker,
                                                          List<Candle> candles) {
        log.info("Processing stock, ticker: " + ticker);
        Map<Processors, Candle> hammers = new HashMap<>();
        if (candles == null || candles.size() < 4) {
            log.warn(String.format("Not enough candles, ticker %s", ticker));
            return hammers;
        }
        var sorted = candles.stream().sorted(Comparator.comparing(Candle::getDatetime)).collect(Collectors.toList());
        Optional.of(sorted.get(sorted.size() - 1))
                .filter(candle -> isPrevDesc(sorted))
                .filter(HammerProcessor::hasBody)
                .filter(HammerProcessor::isUpperPart)
                .filter(HammerProcessor::hasShadow)
                .filter(HammerProcessor::noUpperShadow)
                .ifPresent(candle -> hammers.put(Processors.HAMMER, candle));
        return hammers;
    }

    /**
     * Check if candle has long lower shadow
     *
     * @param candle candle
     * @return hasShadow
     */
    private static boolean hasShadow(Candle candle) {
        return (Math.min(candle.getC(), candle.getO()) - candle.getL()) / Math.abs(candle.getO() - candle.getC())
                >= LOWER_SHADOW_RATIO;
    }

    /**
     * Check if candle has no upper shadow
     *
     * @param candle candle
     * @return hasShadow
     */
    private static boolean noUpperShadow(Candle candle) {
        return Math.abs(candle.getH() - Math.max(candle.getC(), candle.getO())) / Math.abs(candle.getO() - candle.getC())
                <= UPPER_SHADOW_RATIO;
    }

    /**
     * Check if candle has body
     *
     * @param candle candle to process
     * @return hasBody
     */
    private static boolean hasBody(Candle candle) {
        var body = Math.abs(candle.getO() - candle.getC());

        var range = Range.between(LOWER_CANDLE_BODY_RATIO, UPPER_CANDLE_BODY_RATIO);
        return range.contains(body / candle.getC() * 100);
    }

    /**
     * Check if candle has long lower shadow
     *
     * @param candle candle to process
     * @return isUpperPart
     */
    private static boolean isUpperPart(Candle candle) {
        var middle = (candle.getH() - candle.getL()) / 2 + candle.getL();
        return candle.getO() >= middle;
    }

    private static boolean isPrevDesc(List<Candle> candles) {
        var trend = extractTrend(
                candles.get(candles.size() - 4),
                candles.get(candles.size() - 3),
                candles.get(candles.size() - 2)
        );
        return trend != null && trend != Trend.ASCENDING;
    }

    private static Trend extractTrend(Candle first, Candle second, Candle third) {
        Trend result = null;
        if (first.getC() > first.getO() && second.getC() > second.getO()
                && third.getC() > third.getO()) {
            result = Trend.ASCENDING;
        } else if (first.getC() < first.getO() && second.getC() < second.getO()
                && third.getC() < third.getO()) {
            result = Trend.DESCENDING;
        }
        return result;
    }

}
