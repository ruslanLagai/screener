package com.home.project.stocks.processor;

import java.time.LocalDateTime;
import java.util.List;

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
        var candles = List.of(
                generateCandle(20.5, 22.5, 22.6, 18, 5, LocalDateTime.now()),
                generateCandle(20.9, 20.6, 22, 16, 5,  LocalDateTime.now().minusDays(1)),
                generateCandle(21.8, 21.0, 23, 16, 5,  LocalDateTime.now().minusDays(2)),
                generateCandle(22.9, 21.9, 21, 16, 5,  LocalDateTime.now().minusDays(3)),
                generateCandle(20.5, 22.5, 23, 18, 5,  LocalDateTime.now().minusDays(4))
        );

        var result = hammerProcessor.processStock(FIGI, "", candles);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertAll(() -> {
            assertEquals(22.5, result.get(PatternProcessor.Processors.HAMMER).getC());
            assertEquals(20.5, result.get(PatternProcessor.Processors.HAMMER).getO());
            assertEquals(22.6, result.get(PatternProcessor.Processors.HAMMER).getH());
            assertEquals(18, result.get(PatternProcessor.Processors.HAMMER).getL());
            assertEquals(5, result.get(PatternProcessor.Processors.HAMMER).getV());
        });
    }

    @Test
    @DisplayName("Test shadow condition")
    void testShadowCondition() {
        var candles = List.of(
                generateCandle(30.1, 25.2, 31, 24, 10, LocalDateTime.now()),
                generateCandle(25.2, 20.4, 27, 19, 9, LocalDateTime.now().minusDays(1)),
                generateCandle(20.1, 20.5, 21, 20, 5, LocalDateTime.now().minusDays(2)),
                generateCandle(20.5, 22.6, 24, 20, 5, LocalDateTime.now().minusDays(3)),
                generateCandle(22.6, 26.9, 28, 18, 5, LocalDateTime.now().minusDays(4))
        );
        var result = hammerProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test body condition")
    void testBodyCondition() {
        var candles = List.of(
                generateCandle(30.1, 25.2, 31, 24, 10, LocalDateTime.now()),
                generateCandle(25.2, 20.4, 27, 19, 9, LocalDateTime.now().minusDays(1)),
                generateCandle(20.1, 20.5, 21, 20, 5, LocalDateTime.now().minusDays(2)),
                generateCandle(20.5, 21.0, 24, 20, 5, LocalDateTime.now().minusDays(3)),
                generateCandle(22.6, 26.9, 28, 18, 5, LocalDateTime.now().minusDays(4))
        );
        var result = hammerProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test body location")
    void testLocationCondition() {
        var candles = List.of(
                generateCandle(30.1, 25.2, 31, 24, 10, LocalDateTime.now()),
                generateCandle(25.2, 20.4, 27, 19, 9, LocalDateTime.now().minusDays(1)),
                generateCandle(20.1, 20.5, 21, 20, 5, LocalDateTime.now().minusDays(2)),
                generateCandle(20.5, 23.0, 29, 19, 5, LocalDateTime.now().minusDays(3)),
                generateCandle(22.6, 26.9, 28, 18, 5, LocalDateTime.now().minusDays(4))
        );
        var result = hammerProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test not enough candles")
    void testNoCandlesCondition() {
        var candles = List.of(
                generateCandle(25.2, 20.4, 27, 19, 9, LocalDateTime.now()),
                generateCandle(20.1, 20.5, 21, 20, 5, LocalDateTime.now().minusDays(1)),
                generateCandle(20.5, 23.0, 29, 19, 5, LocalDateTime.now().minusDays(2))
        );
        var result = hammerProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }
}