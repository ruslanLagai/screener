package com.home.project.stocks.processor;

import com.home.project.stocks.model.api.EmaPeriod;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.model.entity.DailyEma;
import com.home.project.stocks.service.CandlesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Class to test {@link EmaProcessor}
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test EMA processing")
class EmaProcessorTest extends AbstractProcessorTest {

    @Mock
    private CandlesService candlesService;
    @InjectMocks
    private Ema200Processor ema200Processor;
    @InjectMocks
    private Ema1000Processor ema1000Processor;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(ema200Processor, "threshold", 0.05);
        ReflectionTestUtils.setField(ema1000Processor, "threshold", 0.05);
    }

    @Test
    @DisplayName("test ema - coming to resistance")
    void processIndicator() {
        List<DailyEma> indicatorData = List.of(
                DailyEma.builder()
                        .emaValue(146.8841)
                        .datetime(LocalDate.parse("2021-07-22").atTime(23, 59))
                        .build(),
                DailyEma.builder()
                        .emaValue(148.6213)
                        .datetime(LocalDate.parse("2021-07-21").atTime(23, 59))
                        .build(),
                DailyEma.builder()
                        .emaValue(149.9468)
                        .datetime(LocalDate.parse("2021-07-20").atTime(23, 59))
                        .build(),
                DailyEma.builder()
                        .emaValue(150.2937)
                        .datetime(LocalDate.parse("2021-07-19").atTime(23, 59))
                        .build()
        );

        var candle = generateCandle(140.9, 141.34, 141.7, 140.33, 10, LocalDateTime.now());
        var processingResult = new ProcessingResult();
        var parsedIndicator = ParsedIndicator.builder()
                .ema(indicatorData)
                .ticker("IBM")
                .build();
        when(candlesService.getHistoricalCandles(eq("IBM"), any(), eq(25))).thenReturn(mockCandles(148.2));

        ema200Processor.processIndicator(parsedIndicator, candle, processingResult);
        assertAll(() -> {
            assertTrue(processingResult.getEmaValue().containsKey(EmaPeriod.TWO_HUNDRED));
            assertEquals(146.8841, processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).getEmaValue());
            assertEquals(ProcessingResult.LevelType.RESISTANCE,
                    processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).getLevelType());
            assertEquals(Math.abs(146.8841 - 141.7) / 141.7, processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).getDifference());
            assertTrue(processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).isCloseToEma());
            assertTrue(processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).isCloseRetest());
        });

        var processingResult1000 = new ProcessingResult();
        ema1000Processor.processIndicator(parsedIndicator, candle, processingResult1000);
        assertAll(() -> {
            assertTrue(processingResult1000.getEmaValue().containsKey(EmaPeriod.ONE_THOUSAND));
            assertEquals(146.8841, processingResult1000.getEmaValue().get(EmaPeriod.ONE_THOUSAND).getEmaValue());
            assertEquals(ProcessingResult.LevelType.RESISTANCE,
                    processingResult1000.getEmaValue().get(EmaPeriod.ONE_THOUSAND).getLevelType());
            assertEquals(Math.abs(146.8841 - 141.7) / 141.7, processingResult1000.getEmaValue().get(EmaPeriod.ONE_THOUSAND).getDifference());
            assertTrue(processingResult1000.getEmaValue().get(EmaPeriod.ONE_THOUSAND).isCloseToEma());
            assertTrue(processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).isCloseRetest());
        });
    }

    @Test
    @DisplayName("test ema - coming to support")
    void testSupport() {
        List<DailyEma> indicatorData = List.of(
                DailyEma.builder()
                        .emaValue(146.8841)
                        .datetime(LocalDate.parse("2021-07-22").atTime(23, 59))
                        .build(),
                DailyEma.builder()
                        .emaValue(148.6213)
                        .datetime(LocalDate.parse("2021-07-21").atTime(23, 59))
                        .build(),
                DailyEma.builder()
                        .emaValue(149.9468)
                        .datetime(LocalDate.parse("2021-07-20").atTime(23, 59))
                        .build(),
                DailyEma.builder()
                        .emaValue(150.2937)
                        .datetime(LocalDate.parse("2021-07-19").atTime(23, 59))
                        .build()
        );

        var candle = generateCandle(150.9, 148.34, 151.7, 148.3, 10, LocalDateTime.now());
        var processingResult = new ProcessingResult();
        var parsedIndicator = ParsedIndicator.builder()
                .ticker("IBM")
                .ema(indicatorData)
                .build();
        when(candlesService.getHistoricalCandles(eq("IBM"), any(), eq(25))).thenReturn(mockCandles(151.5));

        ema200Processor.processIndicator(parsedIndicator, candle, processingResult);
        assertAll(() -> {
            assertTrue(processingResult.getEmaValue().containsKey(EmaPeriod.TWO_HUNDRED));
            assertEquals(146.8841, processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).getEmaValue());
            assertEquals(ProcessingResult.LevelType.SUPPORT,
                    processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).getLevelType());
            assertEquals(Math.abs(146.8841 - 148.3) / 148.3, processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).getDifference());
            assertTrue(processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).isCloseToEma());
            assertFalse(processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).isCloseRetest());
        });

        var processingResult1000 = new ProcessingResult();
        ema1000Processor.processIndicator(parsedIndicator, candle, processingResult1000);
        assertAll(() -> {
            assertTrue(processingResult1000.getEmaValue().containsKey(EmaPeriod.ONE_THOUSAND));
            assertEquals(146.8841, processingResult1000.getEmaValue().get(EmaPeriod.ONE_THOUSAND).getEmaValue());
            assertEquals(ProcessingResult.LevelType.SUPPORT,
                    processingResult1000.getEmaValue().get(EmaPeriod.ONE_THOUSAND).getLevelType());
            assertEquals(Math.abs(146.8841 - 148.3) / 148.3, processingResult1000.getEmaValue().get(EmaPeriod.ONE_THOUSAND).getDifference());
            assertTrue(processingResult1000.getEmaValue().get(EmaPeriod.ONE_THOUSAND).isCloseToEma());
            assertFalse(processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).isCloseRetest());
        });
    }

    private List<Candle> mockCandles(double value) {
        List<Candle> candles = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            candles.add(Candle.builder()
                    .c(value)
                    .o(value)
                    .l(value)
                    .h(value)
                    .build());
        }
        return candles;
    }
}