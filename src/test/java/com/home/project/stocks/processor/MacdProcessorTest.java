package com.home.project.stocks.processor;

import com.home.project.stocks.mapper.MacdDataMapper;
import com.home.project.stocks.model.api.CommonIndicator;
import com.home.project.stocks.model.candles.TwelveDataCandles;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.parser.TwelveDataParser;
import com.home.project.stocks.service.CandlesService;
import com.home.project.stocks.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Class to test {@link MacdProcessor}
 */
@ExtendWith(MockitoExtension.class)
class MacdProcessorTest extends AbstractProcessorTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CandlesService candlesService = mock(CandlesService.class);
    private final MacdDataMapper macdDataMapper = new MacdDataMapper();

    @InjectMocks
    private MacdProcessor macdProcessor = new MacdProcessor(candlesService, macdDataMapper);

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(macdProcessor, "columnsNumber", 2);
    }

    @DisplayName("Macd processor - asc divergence")
    @Test
    void testProcessIndicator() {
        var macd = TestUtils.readData("templates/macd/macdData/asc-divergence.json", CommonIndicator.class);
        var candles =  TestUtils.readData("templates/macd/candles/asc-divergence.json", TwelveDataCandles.class).getValues();
        var parsedIndicator = TwelveDataParser.parseMacd(macd);
        var macdData = TwelveDataParser.convertToParsedIndicator(parsedIndicator);
        var procResult = new ProcessingResult();
        procResult.setTicker("LMT");

        when(candlesService.getHistoricalCandles(eq("LMT"), any(), anyInt())).thenReturn(candles);
        macdProcessor.processIndicator(macdData, null, procResult);

        assertAll(() -> {
            assertEquals(395.20001, procResult.getClosePrice());
            assertEquals(ProcessingResult.Trend.NO_SIGN, procResult.getMacdSignalTrend());
            assertEquals(ProcessingResult.Trend.ASCENDING, procResult.getMacdBarTrend());
            assertEquals(ProcessingResult.Trend.ASCENDING, procResult.getMacdDivergence());
        });
    }

    @DisplayName("Macd processor - hill is incomplete")
    @Test
    void testProcessIndicatorDesc() {
        var macd = TestUtils.readData("templates/macd/macdData/desc-divergence.json", CommonIndicator.class);
        var candles =  TestUtils.readData("templates/macd/candles/desc-divergence.json", TwelveDataCandles.class).getValues();
        var parsedIndicator = TwelveDataParser.parseMacd(macd);
        var macdData = TwelveDataParser.convertToParsedIndicator(parsedIndicator);
        var procResult = new ProcessingResult();
        procResult.setTicker("KNSL");

        when(candlesService.getHistoricalCandles(eq("KNSL"), any(), anyInt())).thenReturn(candles);
        macdProcessor.processIndicator(macdData, null, procResult);

        assertAll(() -> {
            assertEquals(264.17999, procResult.getClosePrice());
            assertEquals(ProcessingResult.Trend.NO_SIGN, procResult.getMacdSignalTrend());
            assertEquals(ProcessingResult.Trend.ASCENDING, procResult.getMacdBarTrend());
            assertEquals(ProcessingResult.Trend.NO_SIGN, procResult.getMacdDivergence());
        });
    }

    @DisplayName("Macd processor - no divergence")
    @Test
    void testProcessIndicatorNoDiv() {
        var macd = TestUtils.readData("templates/macd/macdData/no-divergence.json", CommonIndicator.class);
        var candles =  TestUtils.readData("templates/macd/candles/no-divergence.json", TwelveDataCandles.class).getValues();
        var parsedIndicator = TwelveDataParser.parseMacd(macd);
        var macdData = TwelveDataParser.convertToParsedIndicator(parsedIndicator);
        var procResult = new ProcessingResult();
        procResult.setTicker("GOOG");

        when(candlesService.getHistoricalCandles(eq("GOOG"), any(), anyInt())).thenReturn(candles);
        macdProcessor.processIndicator(macdData, null, procResult);

        assertAll(() -> {
            assertEquals(122.65, procResult.getClosePrice());
            assertEquals(ProcessingResult.Trend.NO_SIGN, procResult.getMacdSignalTrend());
            assertEquals(ProcessingResult.Trend.NO_SIGN, procResult.getMacdBarTrend());
            assertEquals(ProcessingResult.Trend.NO_SIGN, procResult.getMacdDivergence());
        });
    }

    @DisplayName("Macd processor - small bars")
    @Test
    void testProcessIndicatorNoDivSmallBars() {
        var macd = TestUtils.readData("templates/macd/macdData/small-bars.json", CommonIndicator.class);
        var candles =  TestUtils.readData("templates/macd/candles/small-bars.json", TwelveDataCandles.class).getValues();
        var parsedIndicator = TwelveDataParser.parseMacd(macd);
        var macdData = TwelveDataParser.convertToParsedIndicator(parsedIndicator);
        var procResult = new ProcessingResult();
        procResult.setTicker("KO");

        when(candlesService.getHistoricalCandles(eq("KO"), any(), anyInt())).thenReturn(candles);
        macdProcessor.processIndicator(macdData, null, procResult);

        assertAll(() -> {
            assertEquals(63.7, procResult.getClosePrice());
            assertEquals(ProcessingResult.Trend.NO_SIGN, procResult.getMacdSignalTrend());
            assertEquals(ProcessingResult.Trend.NO_SIGN, procResult.getMacdBarTrend());
            assertEquals(ProcessingResult.Trend.NO_SIGN, procResult.getMacdDivergence());
        });
    }

    @DisplayName("Macd processor - last hill incomplete")
    @Test
    void testProcessIndicatorNoDivIncomplete() {
        var macd = TestUtils.readData("templates/macd/macdData/last-hill-incomplete.json", CommonIndicator.class);
        var candles =  TestUtils.readData("templates/macd/candles/last-hill-incomplete.json", TwelveDataCandles.class).getValues();
        var parsedIndicator = TwelveDataParser.parseMacd(macd);
        var macdData = TwelveDataParser.convertToParsedIndicator(parsedIndicator);
        var procResult = new ProcessingResult();
        procResult.setTicker("GS");

        when(candlesService.getHistoricalCandles(eq("GS"), any(), anyInt())).thenReturn(candles);
        macdProcessor.processIndicator(macdData, null, procResult);

        assertAll(() -> {
            assertEquals(353.82001, procResult.getClosePrice());
            assertEquals(ProcessingResult.Trend.NO_SIGN, procResult.getMacdSignalTrend());
            assertEquals(ProcessingResult.Trend.ASCENDING, procResult.getMacdBarTrend());
            assertEquals(ProcessingResult.Trend.NO_SIGN, procResult.getMacdDivergence());
        });
    }
}