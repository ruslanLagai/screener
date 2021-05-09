package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

class DodgeProcessorTest extends AbstractProcessorTest {

    DodgeProcessor dodgeProcessor = new DodgeProcessor();

    @Test
    @DisplayName("Common check")
    void processStock() {
        var candles = new Candle[]{
                generateCandle(30.1, 25.2, 31, 24, 10),
                generateCandle(25.2, 20.4, 27, 19, 9),
                generateCandle(20.1, 20.5, 28, 14, 5),
                generateCandle(20.5, 22.6, 24, 17, 5),
                generateCandle(22.6, 26.9, 28, 18, 5),
        };
        var result = dodgeProcessor.processStock(FIGI, "", candles);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertAll(() -> {
            assertEquals(20.5, result.get(2).getC());
            assertEquals(20.1, result.get(2).getO());
            assertEquals(28, result.get(2).getH());
            assertEquals(14, result.get(2).getL());
            assertEquals(5, result.get(2).getV());
            assertEquals(FIGI, result.get(2).getFigi());
        });
    }

    @Test
    @DisplayName("Test shadow condition")
    void testShadowCondition() {
        var candles = new Candle[]{
                generateCandle(30.1, 25.2, 31, 24, 10),
                generateCandle(25.2, 20.4, 27, 19, 9),
                generateCandle(20.1, 20.5, 21, 20, 5),
                generateCandle(20.5, 22.6, 24, 17, 5),
                generateCandle(22.6, 26.9, 28, 18, 5),
        };
        var result = dodgeProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test trend condition")
    void testTrendCondition() {
        var candles = new Candle[]{
                generateCandle(30.1, 32.2, 36, 24, 10),
                generateCandle(25.2, 20.4, 27, 19, 9),
                generateCandle(20.1, 20.5, 21, 20, 5),
                generateCandle(20.5, 22.6, 24, 17, 5),
                generateCandle(22.6, 26.9, 28, 18, 5),
        };
        var result = dodgeProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());

        candles = new Candle[]{
                generateCandle(30.1, 32.2, 36, 24, 10),
                generateCandle(25.2, 20.4, 27, 19, 9),
                generateCandle(20.1, 20.5, 21, 20, 5),
                generateCandle(20.5, 22.6, 24, 17, 5),
                generateCandle(22.6, 20.9, 28, 18, 5),
        };
        result = dodgeProcessor.processStock(FIGI, "", candles);
        assertTrue(result.isEmpty());
    }
}