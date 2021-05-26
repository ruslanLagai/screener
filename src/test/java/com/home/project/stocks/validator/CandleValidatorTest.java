package com.home.project.stocks.validator;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.candles.Payload;
import com.home.project.stocks.processor.AbstractProcessorTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Class to test {@link CandleValidator}
 */
class CandleValidatorTest extends AbstractProcessorTest {

    @Test
    @DisplayName("validate - good candles")
    void validatePositive() {
        var candle = generateCandle(0, 0, 1, 2, 3);
        candle.setInterval("day");
        candle.setTime(LocalDateTime.now());
        candle.setFigi("figi");
        var candle1 = generateCandle(0, 0, 1, 2, 3);
        candle1.setInterval("day");
        candle1.setTime(LocalDateTime.now());
        var map = Map.of("figi", new Candle[]{candle});
        CandleValidator.removeInvalid(map);
        assertEquals(1, map.size());
    }

    @Test
    @DisplayName("validate - one bad candle")
    void validateNegative() {

        var candle = generateCandle(0, 0, 1, 2, 3);
        candle.setInterval("day");
        candle.setTime(LocalDateTime.now());
        candle.setFigi("figi");
        var candle1 = generateCandle(0, 0, 1, 2, 3);
        candle1.setTime(LocalDateTime.now());

        var payload1 = new Payload();
        payload1.setFigi("figi");
        payload1.setCandles( new Candle[]{candle});

        var payload2 = new Payload();
        payload2.setFigi("figi1");
        payload2.setCandles( new Candle[]{candle1});

        var map = Stream.of(payload1, payload2).collect(Collectors.toMap(Payload::getFigi, Payload::getCandles));
        CandleValidator.removeInvalid(map);
        assertEquals(1, map.size());
    }
}