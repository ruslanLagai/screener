package com.home.project.stocks.processor;

import java.time.Instant;
import java.time.Period;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Class to test {@link HammerProcessor}
 */
class HammerProcessorTest extends AbstractProcessorTest {

    HammerProcessor hammerProcessor = new HammerProcessor();

    @Test
    @DisplayName("Hammer test")
    void processStock() {
        var candles = Map.of(
                Date.from(Instant.now()), generateCandle(21.6, 22.2, 31, 24, 10),
                Date.from(Instant.now().minus(Period.ofDays(1))), generateCandle(20.1, 21.6, 22, 16, 5),
                Date.from(Instant.now().minus(Period.ofDays(2))), generateCandle(21.9, 20.1, 23, 16, 5),
                Date.from(Instant.now().minus(Period.ofDays(3))), generateCandle(22.9, 21.9, 21, 16, 5),
                Date.from(Instant.now().minus(Period.ofDays(4))), generateCandle(23.9, 22.9, 26, 19, 5)
        );

        var result = hammerProcessor.processStock(FIGI, "", candles);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertAll(() -> {
            assertEquals(21.6, result.get(PatternProcessor.Processors.HAMMER).get(0).getClose());
            assertEquals(20.1, result.get(PatternProcessor.Processors.HAMMER).get(0).getOpen());
            assertEquals(22, result.get(PatternProcessor.Processors.HAMMER).get(0).getHigh());
            assertEquals(16, result.get(PatternProcessor.Processors.HAMMER).get(0).getLow());
            assertEquals(5, result.get(PatternProcessor.Processors.HAMMER).get(0).getVolume());
        });
    }

    @Test
    @DisplayName("Test shadow condition")
    void testShadowCondition() {
        var candles = Map.of(
                Date.from(Instant.now()), generateCandle(30.1, 25.2, 31, 24, 10),
                Date.from(Instant.now().minus(Period.ofDays(1))), generateCandle(25.2, 20.4, 27, 19, 9),
                Date.from(Instant.now().minus(Period.ofDays(2))), generateCandle(20.1, 20.5, 21, 20, 5),
                Date.from(Instant.now().minus(Period.ofDays(3))), generateCandle(20.5, 22.6, 24, 20, 5),
                Date.from(Instant.now().minus(Period.ofDays(4))), generateCandle(22.6, 26.9, 28, 18, 5)
        );
        var result = hammerProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test body condition")
    void testBodyCondition() {
        var candles = Map.of(
                Date.from(Instant.now()), generateCandle(30.1, 25.2, 31, 24, 10),
                Date.from(Instant.now().minus(Period.ofDays(1))), generateCandle(25.2, 20.4, 27, 19, 9),
                Date.from(Instant.now().minus(Period.ofDays(2))), generateCandle(20.1, 20.5, 21, 20, 5),
                Date.from(Instant.now().minus(Period.ofDays(3))), generateCandle(20.5, 21.0, 24, 20, 5),
                Date.from(Instant.now().minus(Period.ofDays(4))), generateCandle(22.6, 26.9, 28, 18, 5)
        );
        var result = hammerProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test body location")
    void testLocationCondition() {
        var candles = Map.of(
                Date.from(Instant.now()), generateCandle(30.1, 25.2, 31, 24, 10),
                Date.from(Instant.now().minus(Period.ofDays(1))), generateCandle(25.2, 20.4, 27, 19, 9),
                Date.from(Instant.now().minus(Period.ofDays(2))), generateCandle(20.1, 20.5, 21, 20, 5),
                Date.from(Instant.now().minus(Period.ofDays(3))), generateCandle(20.5, 23.0, 29, 19, 5),
                Date.from(Instant.now().minus(Period.ofDays(4))), generateCandle(22.6, 26.9, 28, 18, 5)
        );
        var result = hammerProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test not enough candles")
    void testNoCandlesCondition() {
        var candles = Map.of(
                Date.from(Instant.now()), generateCandle(25.2, 20.4, 27, 19, 9),
                Date.from(Instant.now().minus(Period.ofDays(1))), generateCandle(20.1, 20.5, 21, 20, 5),
                Date.from(Instant.now().minus(Period.ofDays(2))), generateCandle(20.5, 23.0, 29, 19, 5)
        );
        var result = hammerProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }
}