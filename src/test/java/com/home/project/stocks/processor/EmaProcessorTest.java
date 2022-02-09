package com.home.project.stocks.processor;

import com.home.project.stocks.model.aplha.vantage.EmaPeriod;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class to test {@link EmaProcessor}
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test EMA processing")
class EmaProcessorTest extends AbstractProcessorTest {

    private final Ema200Processor ema200Processor = new Ema200Processor();
    private final Ema1000Processor ema1000Processor = new Ema1000Processor();

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(ema200Processor, "threshold", 0.05);
        ReflectionTestUtils.setField(ema1000Processor, "threshold", 0.05);
    }

    @Test
    @DisplayName("test ema - coming to resistance")
    void processIndicator() {
        Map<Date, Double> indicatorData = new HashMap<>();
        indicatorData.put(DateTimeParser.parseDate("2021-07-22"), 146.8841);
        indicatorData.put(DateTimeParser.parseDate("2021-07-21"), 148.6213);
        indicatorData.put(DateTimeParser.parseDate("2021-07-20"), 149.9468);
        indicatorData.put(DateTimeParser.parseDate("2021-07-19"), 150.2937);

        var candle = generateCandle(140.9, 141.34, 141.7, 140.33, 10, LocalDateTime.now());
        var processingResult = new ProcessingResult();
        var parsedIndicator = new ParsedIndicator(indicatorData, null, "IBM", "daily");

        ema200Processor.processIndicator(parsedIndicator, candle, processingResult);
        assertAll(() -> {
            assertTrue(processingResult.getEmaValue().containsKey(EmaPeriod.TWO_HUNDRED));
            assertEquals(146.8841, processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).getEmaValue());
            assertEquals(ProcessingResult.LevelType.RESISTANCE,
                    processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).getLevelType());
            assertEquals(Math.abs(146.8841 - 141.7) / 141.7, processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).getDifference());
            assertTrue(processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).isCloseToEma());
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
        });
    }

    @Test
    @DisplayName("test ema - coming to support")
    void testSupport() {
        Map<Date, Double> indicatorData = new HashMap<>();
        indicatorData.put(DateTimeParser.parseDate("2021-07-22"), 146.8841);
        indicatorData.put(DateTimeParser.parseDate("2021-07-21"), 148.6213);
        indicatorData.put(DateTimeParser.parseDate("2021-07-20"), 149.9468);
        indicatorData.put(DateTimeParser.parseDate("2021-07-19"), 150.2937);

        var candle = generateCandle(150.9, 148.34, 151.7, 148.3, 10, LocalDateTime.now());
        var processingResult = new ProcessingResult();
        var parsedIndicator = new ParsedIndicator(indicatorData, null, "IBM", "daily");

        ema200Processor.processIndicator(parsedIndicator, candle, processingResult);
        assertAll(() -> {
            assertTrue(processingResult.getEmaValue().containsKey(EmaPeriod.TWO_HUNDRED));
            assertEquals(146.8841, processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).getEmaValue());
            assertEquals(ProcessingResult.LevelType.SUPPORT,
                    processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).getLevelType());
            assertEquals(Math.abs(146.8841 - 148.3) / 148.3, processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).getDifference());
            assertTrue(processingResult.getEmaValue().get(EmaPeriod.TWO_HUNDRED).isCloseToEma());
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
        });
    }
}