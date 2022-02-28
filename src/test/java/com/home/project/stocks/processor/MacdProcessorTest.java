package com.home.project.stocks.processor;

import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.model.entity.DailyMacd;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class to test {@link MacdProcessor}
 */
@ExtendWith(MockitoExtension.class)
@Disabled
class MacdProcessorTest extends AbstractProcessorTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    MacdProcessor macdProcessor = new MacdProcessor();

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(macdProcessor, "columnsNumber", 3);
    }

    @Test
    @DisplayName("Macd processor - no sign")
    void processIndicator() {
        //given
        var list = List.of(
                DailyMacd.builder()
                        .macdValue(0.0471)
                        .macdValue(-0.4031)
                        .macdHistValue(0.4503)
                        .datetime(LocalDateTime.parse("2021-08-09 15:00:00", FORMATTER))
                        .build(),
                DailyMacd.builder()
                        .macdValue(-0.1210)
                        .macdSignalValue(-0.5157)
                        .macdHistValue(0.3947)
                        .datetime(LocalDateTime.parse("2021-08-09 16:00:00", FORMATTER))
                        .build(),
                DailyMacd.builder()
                        .macdValue(-0.1993)
                        .macdSignalValue(-0.6144)
                        .macdHistValue(0.4151)
                        .datetime(LocalDateTime.parse("2021-08-09 17:00:00", FORMATTER))
                        .build(),
                DailyMacd.builder()
                        .macdValue(-0.2954)
                        .macdSignalValue(-0.7182)
                        .macdHistValue(0.4227)
                        .datetime(LocalDateTime.parse("2021-08-09 18:00:00", FORMATTER))
                        .build()
        );

        var candle = generateCandle(140.9, 141.34, 141.7, 140.33, 10, LocalDateTime.now());
        var processingResult = new ProcessingResult();
        var parsedIndicator = ParsedIndicator.builder()
                .macd(list)
                .ticker("AAPL")
                .interval("1hour")
                .build();

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
        var list = List.of(
                DailyMacd.builder()
                        .macdValue(0.0471)
                        .macdSignalValue(0.1031)
                        .macdHistValue(0.4503)
                        .datetime(LocalDateTime.parse("2021-08-09 15:00:00", FORMATTER))
                        .build(),
                DailyMacd.builder()
                        .macdValue(-0.1210)
                        .macdSignalValue(-0.0209)
                        .macdHistValue(0.3947)
                        .datetime(LocalDateTime.parse("2021-08-09 16:00:00", FORMATTER))
                        .build(),
                DailyMacd.builder()
                        .macdValue(-0.1993)
                        .macdSignalValue(-0.1)
                        .macdHistValue(0.3151)
                        .datetime(LocalDateTime.parse("2021-08-09 17:00:00", FORMATTER))
                        .build(),
                DailyMacd.builder()
                        .macdValue(-0.2954)
                        .macdSignalValue(-0.7182)
                        .macdHistValue(0.4227)
                        .datetime(LocalDateTime.parse("2021-08-09 18:00:00", FORMATTER))
                        .build()
        );

        var candle = generateCandle(140.9, 141.34, 141.7, 140.33, 10, LocalDateTime.now());
        var processingResult = new ProcessingResult();
        var parsedIndicator = ParsedIndicator.builder()
                .ticker("AAPL")
                .interval("1day")
                .macd(list)
                .build();

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
        var list = List.of(
                DailyMacd.builder()
                        .macdValue(0.171)
                        .macdSignalValue(0.2031)
                        .macdHistValue(0.4503)
                        .datetime(LocalDateTime.parse("2021-08-09 15:00:00", FORMATTER))
                        .build(),
                DailyMacd.builder()
                        .macdValue(-0.0210)
                        .macdSignalValue(-0.0309)
                        .macdHistValue(0.4947)
                        .datetime(LocalDateTime.parse("2021-08-09 16:00:00", FORMATTER))
                        .build(),
                DailyMacd.builder()
                        .macdValue(-0.1993)
                        .macdSignalValue(-0.25)
                        .macdHistValue(0.5151)
                        .datetime(LocalDateTime.parse("2021-08-09 17:00:00", FORMATTER))
                        .build(),
                DailyMacd.builder()
                        .macdValue(-0.2954)
                        .macdSignalValue(-0.7182)
                        .macdHistValue(0.5227)
                        .datetime(LocalDateTime.parse("2021-08-09 18:00:00", FORMATTER))
                        .build()
        );

        var candle = generateCandle(140.9, 141.34, 141.7, 140.33, 10, LocalDateTime.now());
        var processingResult = new ProcessingResult();
        var parsedIndicator = ParsedIndicator.builder()
                .ticker("AAPL")
                .interval("1day")
                .macd(list)
                .build();

        //when
        macdProcessor.processIndicator(parsedIndicator, candle, processingResult);

        //then
        assertAll(() -> {
            assertEquals(ProcessingResult.Trend.DESCENDING, processingResult.getMacdSignalTrend());
            assertEquals(ProcessingResult.Trend.DESCENDING, processingResult.getMacdSignalTrend());
        });
    }
}