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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class to test {@link MacdProcessor}
 */
@ExtendWith(MockitoExtension.class)
class MacdProcessorTest extends AbstractProcessorTest {

    MacdProcessor macdProcessor = new MacdProcessor();

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(macdProcessor, "columnsNumber", 3);
    }

    @Test
    @DisplayName("Macd processor - no sign")
    void processIndicator() {
        //given
        Map<Date, Map<String, Double>> indicatorData = new HashMap<>();
        indicatorData.put(DateTimeParser.parseDate("2021-08-06"), Map.of(
                "MACD", 0.0471,
                "MACD_Signal", -0.4031,
                "MACD_Hist", 0.4503));
        indicatorData.put(DateTimeParser.parseDate("2021-08-05"), Map.of(
                "MACD", -0.1210,
                "MACD_Signal", -0.5157,
                "MACD_Hist", 0.3947));
        indicatorData.put(DateTimeParser.parseDate("2021-08-04"), Map.of(
                "MACD", -0.1993,
                "MACD_Signal", -0.6144,
                "MACD_Hist", 0.4151));
        indicatorData.put(DateTimeParser.parseDate("2021-08-03"), Map.of(
                "MACD", -0.2954,
                "MACD_Signal", -0.7182,
                "MACD_Hist", 0.4227));

        var candle = generateCandle(140.9, 141.34, 141.7, 140.33, 10);
        var processingResult = new ProcessingResult();
        var parsedIndicator = new ParsedIndicator(null, indicatorData, "IBM", "daily");

        //when
        macdProcessor.processIndicator(parsedIndicator, candle, processingResult);

        //then
        assertAll(() -> {
            assertEquals(ProcessingResult.Trend.NO_SIGN, processingResult.getMacdSignalTrend());
            assertEquals(ProcessingResult.Trend.NO_SIGN, processingResult.getMacdSignalTrend());
        });
    }

    @Test
    @DisplayName("Macd processor - asc trend on bar & macd")
    void processIndicatorAscBoth() {
        //given
        Map<Date, Map<String, Double>> indicatorData = new HashMap<>();
        indicatorData.put(DateTimeParser.parseDate("2021-08-06"), Map.of(
                "MACD", 0.471,
                "MACD_Signal", 0.1031,
                "MACD_Hist", 0.4503));
        indicatorData.put(DateTimeParser.parseDate("2021-08-05"), Map.of(
                "MACD", -0.0210,
                "MACD_Signal", -0.0209,
                "MACD_Hist", 0.3947));
        indicatorData.put(DateTimeParser.parseDate("2021-08-04"), Map.of(
                "MACD", -0.1993,
                "MACD_Signal", -0.1,
                "MACD_Hist", 0.3151));
        indicatorData.put(DateTimeParser.parseDate("2021-08-03"), Map.of(
                "MACD", -0.2954,
                "MACD_Signal", -0.7182,
                "MACD_Hist", 0.4227));

        var candle = generateCandle(140.9, 141.34, 141.7, 140.33, 10);
        var processingResult = new ProcessingResult();
        var parsedIndicator = new ParsedIndicator(null, indicatorData, "IBM", "daily");

        //when
        macdProcessor.processIndicator(parsedIndicator, candle, processingResult);

        //then
        assertAll(() -> {
            assertEquals(ProcessingResult.Trend.ASCENDING, processingResult.getMacdSignalTrend());
            assertEquals(ProcessingResult.Trend.ASCENDING, processingResult.getMacdSignalTrend());
        });
    }

    @Test
    @DisplayName("Macd processor - desc trend on bar & macd")
    void processIndicatorDescBoth() {
        //given
        Map<Date, Map<String, Double>> indicatorData = new HashMap<>();
        indicatorData.put(DateTimeParser.parseDate("2021-08-06"), Map.of(
                "MACD", 0.171,
                "MACD_Signal", 0.2031,
                "MACD_Hist", 0.4503));
        indicatorData.put(DateTimeParser.parseDate("2021-08-05"), Map.of(
                "MACD", -0.0210,
                "MACD_Signal", -0.0309,
                "MACD_Hist", 0.4947));
        indicatorData.put(DateTimeParser.parseDate("2021-08-04"), Map.of(
                "MACD", -0.1993,
                "MACD_Signal", -0.25,
                "MACD_Hist", 0.5151));
        indicatorData.put(DateTimeParser.parseDate("2021-08-03"), Map.of(
                "MACD", -0.2954,
                "MACD_Signal", -0.7182,
                "MACD_Hist", 0.5227));

        var candle = generateCandle(140.9, 141.34, 141.7, 140.33, 10);
        var processingResult = new ProcessingResult();
        var parsedIndicator = new ParsedIndicator(null, indicatorData, "IBM", "daily");

        //when
        macdProcessor.processIndicator(parsedIndicator, candle, processingResult);

        //then
        assertAll(() -> {
            assertEquals(ProcessingResult.Trend.DESCENDING, processingResult.getMacdSignalTrend());
            assertEquals(ProcessingResult.Trend.DESCENDING, processingResult.getMacdSignalTrend());
        });
    }
}