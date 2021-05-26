package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.service.RepositoryService;
import org.apache.commons.compress.utils.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

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
        orchestration.setRepositoryService(repositoryService);
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
        var result = orchestration.processStocks(Map.of("testFigi", candles));
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("test dodge processing")
    void testDodgeProcessing() {
        //given
        var candles = new Candle[]{
            generateCandle(30.1, 25.2, 31, 24, 10),
            generateCandle(25.2, 20.4, 27, 19, 9),
            generateCandle(20.1, 20.5, 28, 14, 5),
            generateCandle(20.5, 22.6, 24, 17, 5),
            generateCandle(22.6, 26.9, 28, 18, 5),
        };
        //when
        doReturn(mockResult(true, false, candles[2])).when(repositoryService).save(any());
        var result = orchestration.processStocks(Map.of(FIGI, candles));
        //then
        assertEquals(1, result.size());
        assertAll(() -> {
            assertTrue(result.iterator().next().getIsDodge());
            assertFalse(result.iterator().next().getIsHammer());
            assertTrue(result.iterator().next().shouldBeSent());
            assertEquals(FIGI, result.iterator().next().getFigi());
        });
    }

    @Test
    @DisplayName("test hammer processing")
    void testHammerProcessing() {
        //given
        var candles = new Candle[]{
            generateCandle(30.1, 25.2, 31, 24, 10),
            generateCandle(25.2, 20.4, 27, 19, 9),
            generateCandle(20.1, 18.6, 23, 16, 5),
            generateCandle(18.6, 20.9, 21, 16, 5),
            generateCandle(20.9, 23.9, 26, 19, 5),
        };
        //when
        doReturn(mockResult(false, true, candles[2])).when(repositoryService).save(any());
        //then
        var result = orchestration.processStocks(Map.of(FIGI, candles));
        assertEquals(1, result.size());
        assertAll(() -> {
            assertFalse(result.iterator().next().getIsDodge());
            assertTrue(result.iterator().next().getIsHammer());
            assertTrue(result.iterator().next().shouldBeSent());
            assertEquals(FIGI, result.iterator().next().getFigi());
        });
    }

    @Test
    @DisplayName("test hammer & dodge processing")
    void testTwoPatterndProcessing() {
        //given
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
        doReturn(mockResult(true, true, candles[2])).when(repositoryService).save(any());
        var result = orchestration.processStocks(Map.of(FIGI, candles));
        //then
        assertEquals(1, result.size());
        var list = new ArrayList<>(result);
        assertAll(() -> {
            assertTrue(list.get(0).getIsDodge());
            assertTrue(list.get(0).getIsHammer());
            assertTrue(list.get(0).shouldBeSent());
            assertEquals(FIGI, list.get(0).getFigi());
        });
    }

    @Test
    @DisplayName("test single candle")
    void testSingleCandleProcessing() {
        var candles = new Candle[]{
            generateCandle(30.1, 25.2, 31, 24, 10)
        };
        var result = orchestration.processStocks(Map.of(FIGI, candles));
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("test null entry")
    void testNullProcessing() {
        assertThrows(NullPointerException.class, () -> orchestration.processStocks(null));
    }

    private HashSet<ProcessingResult> mockResult(boolean isDodge, boolean isHammer, Candle candle) {
        var processingResult = new ProcessingResult();
        processingResult.setFigi(FIGI);
        processingResult.setIsDodge(isDodge);
        processingResult.setIsHammer(isHammer);
        MultiValueMap<StocksProcessor.Processors, Candle> candles = new LinkedMultiValueMap<>();
        if (isDodge) {
            candles.add(StocksProcessor.Processors.DODGE, candle);
        } else {
            candles.add(StocksProcessor.Processors.HAMMER, candle);
        }
        processingResult.setProcessedCandles(candles);

        return Sets.newHashSet(processingResult);
    }

    @TestConfiguration
    @ComponentScan(basePackages = {"com.home.project.stocks.processor", "com.home.project.stocks.service",
            "com.home.project.stocks.repository"})
    static class Config {

    }
}