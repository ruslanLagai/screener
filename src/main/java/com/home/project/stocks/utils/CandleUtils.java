package com.home.project.stocks.utils;

import com.home.project.stocks.model.candles.Candle;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * @author rlagay
 */
public class CandleUtils {

    public static List<Candle> getCandlesForPeriod(List<Candle> candles, LocalDateTime start, LocalDateTime end) {
        return candles.stream()
            .sorted(Comparator.comparing(Candle::getDatetime))
            .filter(candle -> candle.getDatetime().isAfter(start) || candle.getDatetime().isEqual(start))
            .filter(candle -> candle.getDatetime().isBefore(end) || candle.getDatetime().isEqual(start))
            .toList();
    }
}
