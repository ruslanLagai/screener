package com.home.project.stocks.processor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class to test {@link EngulfingProcessor}
 */
class EngulfingProcessorTest extends AbstractProcessorTest {

    EngulfingProcessor processor = new EngulfingProcessor();

    @Test
    @DisplayName("Common check")
    void processStock() {
        var candles = List.of(
                generateCandle(31.4, 33.4, 33.6, 31, 10, LocalDateTime.now()),
                generateCandle(33.2, 31.4, 27, 19, 9, LocalDateTime.now().minus(Period.ofDays(1))),
                generateCandle(35.1, 33, 28, 14, 5, LocalDateTime.now().minus(Period.ofDays(2))),
                generateCandle(37.5, 35.6, 24, 17, 5, LocalDateTime.now().minus(Period.ofDays(3))),
                generateCandle(22.6, 26.9, 28, 18, 5, LocalDateTime.now().minus(Period.ofDays(4))));
        var result = processor.processStock(FIGI, "", candles);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertAll(() -> {
            assertEquals(33.4, result.get(PatternProcessor.Processors.ENGULFING).getC());
            assertEquals(31.4, result.get(PatternProcessor.Processors.ENGULFING).getO());
            assertEquals(33.6, result.get(PatternProcessor.Processors.ENGULFING).getH());
            assertEquals(31, result.get(PatternProcessor.Processors.ENGULFING).getL());
            assertEquals(10, result.get(PatternProcessor.Processors.ENGULFING).getV());
        });
    }

    @Test
    @DisplayName("Test shadow condition")
    void testShadowCondition() {
        var candles = List.of(
                generateCandle(30.1, 25.2, 31, 24, 10, LocalDateTime.now()),
                generateCandle(25.2, 20.4, 27, 19, 9, LocalDateTime.now().minus(Period.ofDays(1))),
                generateCandle(20.1, 20.5, 21, 20, 5, LocalDateTime.now().minus(Period.ofDays(2))),
                generateCandle(20.5, 22.6, 24, 17, 5, LocalDateTime.now().minus(Period.ofDays(3))),
                generateCandle(22.6, 26.9, 28, 18, 5, LocalDateTime.now().minus(Period.ofDays(4)))
        );
        var result = processor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test trend condition")
    void testTrendCondition() {

        var candles = List.of(
                generateCandle(30.1, 30.2, 36, 24, 10, LocalDateTime.now()),
                generateCandle(30.1, 29.2, 36, 24, 10, LocalDateTime.now().minus(Period.ofDays(1))),
                generateCandle(22.1, 23.5, 21, 20, 5, LocalDateTime.now().minus(Period.ofDays(2))),
                generateCandle(20.5, 22.6, 24, 17, 5, LocalDateTime.now().minus(Period.ofDays(3))),
                generateCandle(22.6, 22.7, 27, 18, 5, LocalDateTime.now().minus(Period.ofDays(4)))
        );
        var result = processor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());

        candles = List.of(
                generateCandle(30.1, 30.2, 36, 24, 10, LocalDateTime.now()),
                generateCandle(35.2, 36.4, 27, 19, 9, LocalDateTime.now().minus(Period.ofDays(1))),
                generateCandle(40.1, 35.5, 21, 20, 5, LocalDateTime.now().minus(Period.ofDays(2))),
                generateCandle(20.5, 22.6, 24, 17, 5, LocalDateTime.now().minus(Period.ofDays(3))),
                generateCandle(22.6, 20.9, 28, 18, 5, LocalDateTime.now().minus(Period.ofDays(4)))
        );
        result = processor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }
}