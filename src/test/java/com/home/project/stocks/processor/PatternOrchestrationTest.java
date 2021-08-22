package com.home.project.stocks.processor;

import com.home.project.stocks.client.AlphaVantageApiClient;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.service.NotifierService;
import com.home.project.stocks.service.RepositoryService;
import org.apache.commons.compress.utils.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class to test {@link PatternOrchestration}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PatternOrchestrationTest.Config.class})
class PatternOrchestrationTest extends AbstractProcessorTest {

    @MockBean
    RepositoryService repositoryService;

    PatternOrchestration orchestration = new PatternOrchestration();

    @BeforeEach
    public void setUp() {
        orchestration.setStocksProcessors(Arrays.asList(new DodgeProcessor(), new HammerProcessor()));
    }

    @Test
    @DisplayName("single candle -> no result")
    void processStocks() {
        Candle c1 = new Candle();
        c1.setC(1.0);
        c1.setH(2.0);
        c1.setL(4.0);
        c1.setO(5.0);
        c1.setV(6.0);
        c1.setFigi("testFigi");
        c1.setInterval("1min");
        Candle[] candles = {c1};
        var result = new ProcessingResult();
        orchestration.processStocks("testTicker", "testFigi", candles, null, result);
        assertFalse(result.getIsDodge());
        assertFalse(result.getIsHammer());
    }

    @Test
    @DisplayName("test dodge processing")
    void testDodgeProcessing() {
        //given
        var result = new ProcessingResult();
        var candles = new Candle[]{
            generateCandle(30.1, 25.2, 31, 24, 10),
            generateCandle(25.2, 20.4, 27, 19, 9),
            generateCandle(20.1, 20.5, 28, 14, 5),
            generateCandle(20.5, 22.6, 24, 17, 5),
            generateCandle(22.6, 26.9, 28, 18, 5),
        };
        //when
        orchestration.processStocks(TICKER, FIGI, candles, null, result);
        //then
        assertAll(() -> {
            assertTrue(result.getIsDodge());
            assertFalse(result.getIsHammer());
            assertEquals(FIGI, result.getFigi());
        });
    }

    @Test
    @DisplayName("test hammer processing")
    void testHammerProcessing() {
        //given
        var result = new ProcessingResult();
        var candles = new Candle[]{
            generateCandle(30.1, 25.2, 31, 24, 10),
            generateCandle(25.2, 20.4, 27, 19, 9),
            generateCandle(20.1, 18.6, 23, 16, 5),
            generateCandle(18.6, 20.9, 21, 16, 5),
            generateCandle(20.9, 23.9, 26, 19, 5),
        };
        //when
        //then
        orchestration.processStocks(TICKER, FIGI, candles, null, result);
        assertAll(() -> {
            assertFalse(result.getIsDodge());
            assertTrue(result.getIsHammer());
            assertEquals(FIGI, result.getFigi());
        });
    }

    @Test
    @DisplayName("test hammer & dodge processing")
    void testTwoPatterndProcessing() {
        //given
        var result = new ProcessingResult();
        var candles = new Candle[]{
                generateCandle(30.1, 25.2, 31, 24, 10),
                generateCandle(25.2, 20.4, 27, 19, 9),
                generateCandle(20.1, 18.6, 23, 16, 5),
                generateCandle(18.6, 20.9, 21, 16, 5),
                generateCandle(20.9, 23.9, 26, 19, 5),
                generateCandle(30.1, 25.2, 31, 24, 10),
                generateCandle(25.2, 20.4, 27, 19, 9),
                generateCandle(20.1, 20.5, 28, 14, 5),
                generateCandle(20.5, 22.6, 24, 17, 5),
                generateCandle(22.6, 26.9, 28, 18, 5)
        };
        //when
        orchestration.processStocks(TICKER, FIGI, candles, null, result);
        //then
        assertAll(() -> {
            assertTrue(result.getIsDodge());
            assertTrue(result.getIsHammer());
            assertEquals(FIGI, result.getFigi());
        });
    }

    @Test
    @DisplayName("test single candle")
    void testSingleCandleProcessing() {
        var result = new ProcessingResult();
        var candles = new Candle[]{
            generateCandle(30.1, 25.2, 31, 24, 10)
        };
        orchestration.processStocks(TICKER, FIGI, candles, null, result);
        assertFalse(result.getIsDodge());
        assertFalse(result.getIsHammer());
    }

    @Test
    @DisplayName("test null entry")
    void testNullProcessing() {
        assertThrows(NullPointerException.class,
                () -> orchestration.processStocks(null, null, null, null, null));
    }

    private HashSet<ProcessingResult> mockResult(boolean isDodge, boolean isHammer, Candle candle) {
        var processingResult = new ProcessingResult();
        processingResult.setFigi(FIGI);
        processingResult.setIsDodge(isDodge);
        processingResult.setIsHammer(isHammer);
        MultiValueMap<PatternProcessor.Processors, Candle> candles = new LinkedMultiValueMap<>();
        if (isDodge) {
            candles.add(PatternProcessor.Processors.DODGE, candle);
        } else {
            candles.add(PatternProcessor.Processors.HAMMER, candle);
        }
        processingResult.setProcessedCandles(candles);

        return Sets.newHashSet(processingResult);
    }

    @TestConfiguration
    @ComponentScan(basePackages = {"com.home.project.stocks.service"})
    static class Config {

        @Bean
        DodgeProcessor dodgeProcessor() {
            return new DodgeProcessor();
        };

        @Bean
        HammerProcessor hammerProcessor() {
            return new HammerProcessor();
        };

        @MockBean
        AlphaVantageApiClient alphaVantageApiClient;

        @MockBean
        NotifierService notifierService;
    }
}