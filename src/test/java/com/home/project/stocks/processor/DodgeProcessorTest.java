package com.home.project.stocks.processor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class to test {@link DodgeProcessor}
 */
class DodgeProcessorTest extends AbstractProcessorTest {

    DodgeProcessor dodgeProcessor = new DodgeProcessor();

    @Test
    @DisplayName("Common check")
    void processStock() {
        var candles = List.of(
                generateCandle(30.1, 30.2, 33, 27, 10, LocalDateTime.now()),
                generateCandle(33.2, 31.4, 27, 19, 9, LocalDateTime.now().minus(Period.ofDays(1))),
                generateCandle(35.1, 33, 28, 14, 5, LocalDateTime.now().minus(Period.ofDays(2))),
                generateCandle(20.5, 22.6, 24, 17, 5, LocalDateTime.now().minus(Period.ofDays(3))),
                generateCandle(22.6, 26.9, 28, 18, 5, LocalDateTime.now().minus(Period.ofDays(4))));
        var result = dodgeProcessor.processStock(FIGI, "", candles);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertAll(() -> {
            assertEquals(30.2, result.get(PatternProcessor.Processors.DODGE).getC());
            assertEquals(30.1, result.get(PatternProcessor.Processors.DODGE).getO());
            assertEquals(33, result.get(PatternProcessor.Processors.DODGE).getH());
            assertEquals(27, result.get(PatternProcessor.Processors.DODGE).getL());
            assertEquals(10, result.get(PatternProcessor.Processors.DODGE).getV());
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
        var result = dodgeProcessor.processStock(FIGI, "", candles);
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
        var result = dodgeProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());

        candles = List.of(
                generateCandle(30.1, 30.2, 36, 24, 10, LocalDateTime.now()),
                generateCandle(35.2, 36.4, 27, 19, 9, LocalDateTime.now().minus(Period.ofDays(1))),
                generateCandle(40.1, 35.5, 21, 20, 5, LocalDateTime.now().minus(Period.ofDays(2))),
                generateCandle(20.5, 22.6, 24, 17, 5, LocalDateTime.now().minus(Period.ofDays(3))),
                generateCandle(22.6, 20.9, 28, 18, 5, LocalDateTime.now().minus(Period.ofDays(4)))
        );
        result = dodgeProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }
}