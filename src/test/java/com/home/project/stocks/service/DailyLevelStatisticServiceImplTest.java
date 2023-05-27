package com.home.project.stocks.service;

import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.model.candles.TwelveDataCandles;
import com.home.project.stocks.model.entity.ProcessedLevels;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.service.impl.DailyLevelStatisticServiceImpl;
import com.home.project.stocks.utils.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * @author rlagay
 */
@ExtendWith(MockitoExtension.class)
class DailyLevelStatisticServiceImplTest {

    @Mock
    private CandlesService candlesService;

    @InjectMocks
    private DailyLevelStatisticServiceImpl levelStatisticService;

    @Test
    @DisplayName("test support level processing")
    void analyzeStockSupport() {
        var candles =  TestUtils.readData("templates/macd/statistics/tmus-candles.json", TwelveDataCandles.class).getValues();
        when(candlesService.getHistoricalCandles(any(), any(), anyInt())).thenReturn(candles);
        var level = ProcessedLevels.builder().levelType(ProcessingResult.LevelType.SUPPORT).level(129.55).build();
        levelStatisticService.analyzeStock(level, Interval.TWELVE_DATA_ONE_DAY);

        assertEquals(1.0, level.getSuccessRate());
        assertEquals(0.0, level.getAverageBreaking());
        assertEquals(0.03, level.getAverageRebound());
        assertEquals(2, level.getTotalCrosses());
    }

    @Test
    @DisplayName("test resistance level processing")
    void analyzeStockResistance() {
        var candles =  TestUtils.readData("templates/macd/statistics/tmus-candles.json", TwelveDataCandles.class).getValues();
        when(candlesService.getHistoricalCandles(any(), any(), anyInt())).thenReturn(candles);
        var level = ProcessedLevels.builder().levelType(ProcessingResult.LevelType.RESISTANCE).level(129.55).build();
        levelStatisticService.analyzeStock(level, Interval.TWELVE_DATA_ONE_DAY);

        assertEquals(1.0, level.getSuccessRate());
        assertEquals(0.0, level.getAverageBreaking());
        assertEquals(0.04, level.getAverageRebound());
        assertEquals(1, level.getTotalCrosses());
    }

    @Test
    @DisplayName("test resistance historical maximum")
    void analyzeStockMaxinmum() {
        var candles =  TestUtils.readData("templates/macd/statistics/ipg-candles.json", TwelveDataCandles.class).getValues();
        when(candlesService.getHistoricalCandles(any(), any(), anyInt())).thenReturn(candles);
        var level = ProcessedLevels.builder().levelType(ProcessingResult.LevelType.RESISTANCE).level(39.35).build();
        levelStatisticService.analyzeStock(level, Interval.TWELVE_DATA_ONE_DAY);

        assertEquals(1.0, level.getSuccessRate());
        assertEquals(0.02, level.getAverageBreaking());
        assertEquals(0.09, level.getAverageRebound());
        assertEquals(3, level.getTotalCrosses());
    }
}