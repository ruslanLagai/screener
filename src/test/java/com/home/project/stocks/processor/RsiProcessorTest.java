package com.home.project.stocks.processor;

import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.model.entity.DailyRsi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RsiProcessorTest extends AbstractProcessorTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    RsiProcessor rsiProcessor = new RsiProcessor();

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(rsiProcessor, "columnsNumber", 3);
    }

    @Test
    @DisplayName("RSI processor - no sign")
    void processIndicator() {
        var list = List.of(
                DailyRsi.builder()
                        .rsiValue(29.2937)
                        .datetime(LocalDateTime.parse("2021-08-09 15:00:00", FORMATTER))
                        .build(),
                DailyRsi.builder()
                        .rsiValue(29.9468)
                        .datetime(LocalDateTime.parse("2021-08-09 16:00:00", FORMATTER))
                        .build(),
                DailyRsi.builder()
                        .rsiValue(49.6213)
                        .datetime(LocalDateTime.parse("2021-08-09 17:00:00", FORMATTER))
                        .build(),
                DailyRsi.builder()
                        .rsiValue(46.8841)
                        .datetime(LocalDateTime.parse("2021-08-09 18:00:00", FORMATTER))
                        .build()
        );

        var candle = generateCandle(140.9, 141.34, 141.7, 140.33, 10, LocalDateTime.now());

        var processingResult = new ProcessingResult();
        var parsedIndicator = ParsedIndicator.builder().ticker("AAPL").interval("1hour").rsi(list).build();
        rsiProcessor.processIndicator(parsedIndicator, candle, processingResult);
        assertAll(() -> {
            assertEquals(ProcessingResult.RsiSign.NO_SIGN, processingResult.getRsiSign());
            assertEquals(Arrays.asList(46.8841, 49.6213, 29.9468), processingResult.getRsiValues());
        });
    }

    @Test
    @DisplayName("RSI processor - oversold")
    void testOversold() {
        var list = List.of(
                DailyRsi.builder()
                        .rsiValue(30.2937)
                        .datetime(LocalDateTime.parse("2021-08-09 15:00:00", FORMATTER))
                        .build(),
                DailyRsi.builder()
                        .rsiValue(19.9468)
                        .datetime(LocalDateTime.parse("2021-08-09 16:00:00", FORMATTER))
                        .build(),
                DailyRsi.builder()
                        .rsiValue(16.6213)
                        .datetime(LocalDateTime.parse("2021-08-09 17:00:00", FORMATTER))
                        .build(),
                DailyRsi.builder()
                        .rsiValue(14.8841)
                        .datetime(LocalDateTime.parse("2021-08-09 18:00:00", FORMATTER))
                        .build()
        );

        var candle = generateCandle(140.9, 141.34, 141.7, 140.33, 10, LocalDateTime.now());

        var processingResult = new ProcessingResult();
        var parsedIndicator = ParsedIndicator.builder().ticker("AAPL").interval("1hour").rsi(list).build();
        rsiProcessor.processIndicator(parsedIndicator, candle, processingResult);
        assertAll(() -> {
            assertEquals(ProcessingResult.RsiSign.OVERSOLD, processingResult.getRsiSign());
            assertEquals(Arrays.asList(14.8841, 16.6213, 19.9468), processingResult.getRsiValues());
        });
    }

    @Test
    @DisplayName("RSI processor - overbought")
    void testOverbought() {
        var list = List.of(
                DailyRsi.builder()
                        .rsiValue(75.8841)
                        .datetime(LocalDateTime.parse("2021-08-10 15:00:00", FORMATTER))
                        .build(),
                DailyRsi.builder()
                        .rsiValue(74.6213)
                        .datetime(LocalDateTime.parse("2021-08-09 16:00:00", FORMATTER))
                        .build(),
                DailyRsi.builder()
                        .rsiValue(70.9468)
                        .datetime(LocalDateTime.parse("2021-08-08 17:00:00", FORMATTER))
                        .build(),
                DailyRsi.builder()
                        .rsiValue(30.2937)
                        .datetime(LocalDateTime.parse("2021-08-07 18:00:00", FORMATTER))
                        .build()
        );

        var candle = generateCandle(140.9, 141.34, 141.7, 140.33, 10, LocalDateTime.now());

        var processingResult = new ProcessingResult();
        var parsedIndicator = ParsedIndicator.builder().ticker("AAPL").interval("1day").rsi(list).build();
        rsiProcessor.processIndicator(parsedIndicator, candle, processingResult);
        assertAll(() -> {
            assertEquals(ProcessingResult.RsiSign.OVERBOUGHT, processingResult.getRsiSign());
            assertEquals(Arrays.asList(75.8841, 74.6213, 70.9468), processingResult.getRsiValues());
        });
    }
}