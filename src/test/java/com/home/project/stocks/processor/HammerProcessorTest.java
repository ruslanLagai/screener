package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class to test {@link HammerProcessor}
 */
class HammerProcessorTest extends AbstractProcessorTest{

    HammerProcessor hammerProcessor = new HammerProcessor();

    @Test
    @DisplayName("Hammer test")
    void processStock() {
        var candles = new Candle[]{
                generateCandle(30.1, 25.2, 31, 24, 10),
                generateCandle(25.2, 20.4, 27, 19, 9),
                generateCandle(20.1, 18.6, 23, 16, 5),
                generateCandle(18.6, 20.9, 21, 16, 5),
                generateCandle(20.9, 23.9, 26, 19, 5),
        };

        var result = hammerProcessor.processStock(FIGI, "", candles);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertAll(() -> {
            assertEquals(20.9, result.get(3).getC());
            assertEquals(18.6, result.get(3).getO());
            assertEquals(21, result.get(3).getH());
            assertEquals(16, result.get(3).getL());
            assertEquals(5, result.get(3).getV());
            assertEquals(FIGI, result.get(3).getFigi());
        });
    }

    @Test
    @DisplayName("Test shadow condition")
    void testShadowCondition() {
        var candles = new Candle[]{
                generateCandle(30.1, 25.2, 31, 24, 10),
                generateCandle(25.2, 20.4, 27, 19, 9),
                generateCandle(20.1, 20.5, 21, 20, 5),
                generateCandle(20.5, 22.6, 24, 20, 5),
                generateCandle(22.6, 26.9, 28, 18, 5),
        };
        var result = hammerProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test body condition")
    void testBodyCondition() {
        var candles = new Candle[]{
                generateCandle(30.1, 25.2, 31, 24, 10),
                generateCandle(25.2, 20.4, 27, 19, 9),
                generateCandle(20.1, 20.5, 21, 20, 5),
                generateCandle(20.5, 21.0, 24, 20, 5),
                generateCandle(22.6, 26.9, 28, 18, 5),
        };
        var result = hammerProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test body location")
    void testLocationCondition() {
        var candles = new Candle[]{
                generateCandle(30.1, 25.2, 31, 24, 10),
                generateCandle(25.2, 20.4, 27, 19, 9),
                generateCandle(20.1, 20.5, 21, 20, 5),
                generateCandle(20.5, 23.0, 29, 19, 5),
                generateCandle(22.6, 26.9, 28, 18, 5),
        };
        var result = hammerProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test not enough candles")
    void testNoCandlesCondition() {
        var candles = new Candle[]{
                generateCandle(25.2, 20.4, 27, 19, 9),
                generateCandle(20.1, 20.5, 21, 20, 5),
                generateCandle(20.5, 23.0, 29, 19, 5),
        };
        var result = hammerProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }
}