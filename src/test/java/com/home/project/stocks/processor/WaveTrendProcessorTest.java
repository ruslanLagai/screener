package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.candles.TwelveDataCandles;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.service.CandlesService;
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
 * test for {@link WaveTrendProcessor}
 *
 * @author rlagay
 */
@ExtendWith(MockitoExtension.class)
class WaveTrendProcessorTest {

    private final ParsedIndicator indicator = ParsedIndicator.builder().ticker("ticker").build();
    private final Candle candle = Candle.builder().build();

    @Mock
    private CandlesService candlesService;

    @InjectMocks
    private WaveTrendProcessor waveTrendProcessor;

    @Test
    @DisplayName("Test buy sign")
    void processIndicator() {
        var result = new ProcessingResult();
        var candles =  TestUtils.readData("templates/wt/wt-buy.json", TwelveDataCandles.class).getValues();
        candles.remove(0);

        when(candlesService.getHistoricalCandles(any(), any(), anyInt())).thenReturn(candles);
        waveTrendProcessor.processIndicator(indicator, candle, result);

        assertEquals(ProcessingResult.Trend.ASCENDING, result.getWtTrend());
    }

    @Test
    @DisplayName("Test no sign - day after buy sign")
    void processIndicatorDayAfterSign() {
        var result = new ProcessingResult();
        var candles =  TestUtils.readData("templates/wt/wt-buy.json", TwelveDataCandles.class).getValues();

        when(candlesService.getHistoricalCandles(any(), any(), anyInt())).thenReturn(candles);
        waveTrendProcessor.processIndicator(indicator, candle, result);

        assertEquals(ProcessingResult.Trend.NO_SIGN, result.getWtTrend());
    }

    @Test
    @DisplayName("Test no sign")
    void processIndicatorNoSign() {
        var result = new ProcessingResult();
        var candles =  TestUtils.readData("templates/wt/wt-buy.json", TwelveDataCandles.class).getValues();
        candles = candles.subList(0, candles.size() - 10);

        when(candlesService.getHistoricalCandles(any(), any(), anyInt())).thenReturn(candles);
        waveTrendProcessor.processIndicator(indicator, candle, result);

        assertEquals(ProcessingResult.Trend.NO_SIGN, result.getWtTrend());
    }

    @Test
    @DisplayName("Test no sign - sell sign, for now short is disabled")
    void processIndicatorNoSignSellSign() {
        var result = new ProcessingResult();
        var candles =  TestUtils.readData("templates/wt/wt-sell.json", TwelveDataCandles.class).getValues();

        when(candlesService.getHistoricalCandles(any(), any(), anyInt())).thenReturn(candles);
        waveTrendProcessor.processIndicator(indicator, candle, result);

        assertEquals(ProcessingResult.Trend.NO_SIGN, result.getWtTrend());
    }
}