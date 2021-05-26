package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.Trend;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Class to process Hammer pattern
 *
 */
@Component
@Log4j2
public class HammerProcessor implements StocksProcessor {

    private static final double LOWER_SHADOW_RATIO = 1.1;
    private static final double LOWER_CANDLE_BODY_RATIO = 0.1;
    private static final double UPPER_CANDLE_BODY_RATIO = 0.9;

    @Override
    public MultiValueMap<Processors, Candle> processStock(String figi, String ticker, Candle[] candles) {
        log.info("Processing stock, figi: " + figi);
        MultiValueMap<Processors, Candle> hammers = new LinkedMultiValueMap<>();
        Stream.of(candles)
                .filter(candle -> {
                    var index = Arrays.asList(candles).indexOf(candle);
                    var isPrevDesc = isPrevDesc(index, candles);
                    var isNextAsc = isNextAsc(index, candles);
                    return isNextAsc && isPrevDesc;
                })
                .filter(HammerProcessor::hasBody)
                .filter(HammerProcessor::isUpperPart)
                .filter(HammerProcessor::hasShadow)
                .map(candle -> Arrays.asList(candles).indexOf(candle))
                .forEach(index -> hammers.addIfAbsent(Processors.HAMMER, candles[index]));
        return hammers;
    }

    private static boolean hasShadow(Candle candle) {
        return candle.getO() / candle.getL() > LOWER_SHADOW_RATIO;
    }

    private static boolean hasBody(Candle candle) {
        var body = Math.abs(candle.getO() / candle.getC());
        var range = Range.between(LOWER_CANDLE_BODY_RATIO, UPPER_CANDLE_BODY_RATIO);
        return range.contains(body);
    }

    private static boolean isUpperPart(Candle candle) {
        var middle = (candle.getH() - candle.getL()) / 2 + candle.getL();
        return candle.getO() > middle;
    }

    private static boolean isPrevDesc(Integer index, Candle[] candles) {
        if (index < 3) {
            return false;
        }
        var trend = extractTrend(candles[index - 3], candles[index - 2], candles[index - 3]);
        return trend != null && trend != Trend.ASCENDING;
    }

    private static boolean isNextAsc(Integer index, Candle[] candles) {
        if (index >= candles.length - 1) {
            return false;
        }
        return candles[index + 1].getC() - candles[index + 1].getO() > 0;
    }

    private static Trend extractTrend(Candle first, Candle second, Candle third) {
        Trend result = null;
        if (first.getC() > first.getO() && second.getC() > second.getO() && third.getC() > third.getO()) {
            result = Trend.ASCENDING;
        } else if (first.getC() < first.getO() && second.getC() < second.getO() && third.getC() < third.getO()) {
            result = Trend.DESCENDING;
        }
        return result;
    }

}
