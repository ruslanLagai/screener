package com.home.project.stocks.validator;

import com.home.project.stocks.model.candles.Candle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class to validate candles which come from server
 */
public class CandleValidator {

    public static void removeInvalid(Map<String, Candle[]> candles) {
        Set<String> toRemove = new HashSet<>();
        candles.forEach((k,v) -> {
            var result = Arrays.stream(v).filter(CandleValidator::validateCandle).findFirst();
            if (result.isPresent()) {
                toRemove.add(k);
            }
        });
        toRemove.forEach(candles::remove);
    }

    private static boolean validateCandle(Candle candle) {
        return candle.getTime() == null || candle.getInterval() == null || candle.getFigi() == null;
    }
}
