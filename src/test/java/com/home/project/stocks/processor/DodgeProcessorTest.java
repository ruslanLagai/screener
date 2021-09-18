package com.home.project.stocks.processor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.Period;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class to test {@link DodgeProcessor}
 */
class DodgeProcessorTest extends AbstractProcessorTest {

    DodgeProcessor dodgeProcessor = new DodgeProcessor();

    @Test
    @DisplayName("Common check")
    void processStock() {
        var candles = Map.of(
                Date.from(Instant.now()), generateCandle(30.1, 25.2, 31, 24, 10),
                Date.from(Instant.now().minus(Period.ofDays(1))), generateCandle(25.2, 20.4, 27, 19, 9),
                Date.from(Instant.now().minus(Period.ofDays(2))), generateCandle(20.1, 20.5, 28, 14, 5),
                Date.from(Instant.now().minus(Period.ofDays(3))), generateCandle(20.5, 22.6, 24, 17, 5),
                Date.from(Instant.now().minus(Period.ofDays(4))), generateCandle(22.6, 26.9, 28, 18, 5)
        );
        var result = dodgeProcessor.processStock(FIGI, "", candles);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertAll(() -> {
            assertEquals(20.5, result.get(PatternProcessor.Processors.DODGE).get(0).getClose());
            assertEquals(20.1, result.get(PatternProcessor.Processors.DODGE).get(0).getOpen());
            assertEquals(28, result.get(PatternProcessor.Processors.DODGE).get(0).getHigh());
            assertEquals(14, result.get(PatternProcessor.Processors.DODGE).get(0).getLow());
            assertEquals(5, result.get(PatternProcessor.Processors.DODGE).get(0).getVolume());
        });
    }

    @Test
    @DisplayName("Test shadow condition")
    void testShadowCondition() {
        var candles = Map.of(
                Date.from(Instant.now()), generateCandle(30.1, 25.2, 31, 24, 10),
                Date.from(Instant.now().minus(Period.ofDays(1))), generateCandle(25.2, 20.4, 27, 19, 9),
                Date.from(Instant.now().minus(Period.ofDays(2))), generateCandle(20.1, 20.5, 21, 20, 5),
                Date.from(Instant.now().minus(Period.ofDays(3))), generateCandle(20.5, 22.6, 24, 17, 5),
                Date.from(Instant.now().minus(Period.ofDays(4))), generateCandle(22.6, 26.9, 28, 18, 5)
        );
        var result = dodgeProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test trend condition")
    void testTrendCondition() {

        var candles = Map.of(
                Date.from(Instant.now()), generateCandle(30.1, 32.2, 36, 24, 10),
                Date.from(Instant.now().minus(Period.ofDays(1))), generateCandle(25.2, 20.4, 27, 19, 9),
                Date.from(Instant.now().minus(Period.ofDays(2))), generateCandle(20.1, 20.5, 21, 20, 5),
                Date.from(Instant.now().minus(Period.ofDays(3))), generateCandle(20.5, 22.6, 24, 17, 5),
                Date.from(Instant.now().minus(Period.ofDays(4))), generateCandle(22.6, 26.9, 28, 18, 5)
        );
        var result = dodgeProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());

        candles = Map.of(
                Date.from(Instant.now()), generateCandle(30.1, 32.2, 36, 24, 10),
                Date.from(Instant.now().minus(Period.ofDays(1))), generateCandle(25.2, 20.4, 27, 19, 9),
                Date.from(Instant.now().minus(Period.ofDays(2))), generateCandle(20.1, 20.5, 21, 20, 5),
                Date.from(Instant.now().minus(Period.ofDays(3))), generateCandle(20.5, 22.6, 24, 17, 5),
                Date.from(Instant.now().minus(Period.ofDays(4))), generateCandle(22.6, 20.9, 28, 18, 5)
        );
        result = dodgeProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }
}