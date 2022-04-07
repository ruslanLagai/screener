package com.home.project.stocks.processor;

import com.home.project.stocks.utils.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author rlagay
 */
@DisplayName("Test levels detector")
@ExtendWith(MockitoExtension.class)
class WeeklyLevelProcessorTest {

    private final WeeklyLevelProcessor weeklyLevelProcessor = new WeeklyLevelProcessor();

    @Test
    @DisplayName("SPOT test")
    void processStock() {
        var candles = TestUtils.readCandles("templates/levels/spot-candles.json").getValues();
        var result = weeklyLevelProcessor.processStock("SPOT", candles);

        assertEquals(14, result.size());
        assertEquals(387.44, result.stream().mapToDouble(Double::doubleValue).max().orElse(0));
        assertEquals(103.29, result.stream().mapToDouble(Double::doubleValue).min().orElse(0));
    }

    @Test
    @DisplayName("AAPL test")
    void processStockAapl() {
        var candles = TestUtils.readCandles("templates/levels/aapl-candles.json").getValues();
        var result = weeklyLevelProcessor.processStock("AAPL", candles);

        assertEquals(11, result.size());
        assertEquals(182.94, result.stream().mapToDouble(Double::doubleValue).max().orElse(0));
        assertEquals(35.5, result.stream().mapToDouble(Double::doubleValue).min().orElse(0));
    }

    @Test
    @DisplayName("Empty candles test")
    void emptyCandles() {
        var result = weeklyLevelProcessor.processStock("SPOT", Collections.emptyList());
        assertEquals(0, result.size());
    }
}