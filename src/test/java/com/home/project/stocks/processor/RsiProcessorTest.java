package com.home.project.stocks.processor;

import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.utils.DateTimeParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RsiProcessorTest extends AbstractProcessorTest {

    RsiProcessor rsiProcessor = new RsiProcessor();

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(rsiProcessor, "columnsNumber", 3);
    }

    @Test
    @DisplayName("RSI processor - no sign")
    void processIndicator() {
        Map<Date, Double> indicatorData = new HashMap<>();
        indicatorData.put(DateTimeParser.parseDate("2021-07-22"), 46.8841);
        indicatorData.put(DateTimeParser.parseDate("2021-07-21"), 49.6213);
        indicatorData.put(DateTimeParser.parseDate("2021-07-20"), 29.9468);
        indicatorData.put(DateTimeParser.parseDate("2021-07-19"), 29.2937);

        var candle = generateCandle(140.9, 141.34, 141.7, 140.33, 10, LocalDateTime.now());

        var processingResult = new ProcessingResult();
        var parsedIndicator = new ParsedIndicator(indicatorData, null, "IBM", "daily");
        rsiProcessor.processIndicator(parsedIndicator, candle, processingResult);
        assertAll(() -> {
            assertEquals(ProcessingResult.RsiSign.NO_SIGN, processingResult.getRsiSign());
            assertEquals(Arrays.asList(46.8841, 49.6213, 29.9468), processingResult.getRsiValues());
        });
    }

    @Test
    @DisplayName("RSI processor - oversold")
    void testOversold() {
        Map<Date, Double> indicatorData = new HashMap<>();
        indicatorData.put(DateTimeParser.parseDate("2021-07-22"), 14.8841);
        indicatorData.put(DateTimeParser.parseDate("2021-07-21"), 16.6213);
        indicatorData.put(DateTimeParser.parseDate("2021-07-20"), 19.9468);
        indicatorData.put(DateTimeParser.parseDate("2021-07-19"), 30.2937);

        var candle = generateCandle(140.9, 141.34, 141.7, 140.33, 10, LocalDateTime.now());

        var processingResult = new ProcessingResult();
        var parsedIndicator = new ParsedIndicator(indicatorData, null, "IBM", "daily");
        rsiProcessor.processIndicator(parsedIndicator, candle, processingResult);
        assertAll(() -> {
            assertEquals(ProcessingResult.RsiSign.OVERSOLD, processingResult.getRsiSign());
            assertEquals(Arrays.asList(14.8841, 16.6213, 19.9468), processingResult.getRsiValues());
        });
    }

    @Test
    @DisplayName("RSI processor - overbought")
    void testOverbought() {
        Map<Date, Double> indicatorData = new HashMap<>();
        indicatorData.put(DateTimeParser.parseDate("2021-07-22"), 75.8841);
        indicatorData.put(DateTimeParser.parseDate("2021-07-21"), 74.6213);
        indicatorData.put(DateTimeParser.parseDate("2021-07-20"), 70.9468);
        indicatorData.put(DateTimeParser.parseDate("2021-07-19"), 30.2937);

        var candle = generateCandle(140.9, 141.34, 141.7, 140.33, 10, LocalDateTime.now());

        var processingResult = new ProcessingResult();
        var parsedIndicator = new ParsedIndicator(indicatorData, null, "IBM", "daily");
        rsiProcessor.processIndicator(parsedIndicator, candle, processingResult);
        assertAll(() -> {
            assertEquals(ProcessingResult.RsiSign.OVERBOUGHT, processingResult.getRsiSign());
            assertEquals(Arrays.asList(75.8841, 74.6213, 70.9468), processingResult.getRsiValues());
        });
    }
}