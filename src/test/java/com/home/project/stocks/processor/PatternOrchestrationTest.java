package com.home.project.stocks.processor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import com.home.project.stocks.model.candles.Candle;
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

import com.home.project.stocks.client.AlphaVantageApiClient;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.service.RepositoryService;

/**
 * Class to test {@link PatternOrchestration}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PatternOrchestrationTest.Config.class})
class PatternOrchestrationTest extends AbstractProcessorTest {

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
        c1.setTime(LocalDateTime.now());
        var result = new ProcessingResult();
        orchestration.processStocks("testTicker", "testFigi",
                List.of(c1), null, result);
        assertNull(result.getIsDodge());
        assertNull(result.getIsHammer());
    }

    @Test
    @DisplayName("test dodge processing")
    void testDodgeProcessing() {
        //given
        var result = new ProcessingResult();
        var candles = List.of(
                generateCandle(1000.1, 1000.5, 1055, 920, 10, LocalDateTime.now()),
                generateCandle(1040.2, 1002.4, 27, 19, 9, LocalDateTime.now().minusDays(1)),
                generateCandle(1080.1, 1040.5, 28, 14, 5, LocalDateTime.now().minusDays(2)),
                generateCandle(1100.5, 1080.6, 24, 17, 5, LocalDateTime.now().minusDays(3)),
                generateCandle(22.6, 26.9, 28, 18, 5, LocalDateTime.now().minusDays(4))
        );
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
    @DisplayName("test hammer processing - big numbers")
    void testHammerProcessing1() {
        //given
        var result = new ProcessingResult();
        var candles = List.of(
                generateCandle(1000.1, 1050.2, 1055, 920, 10, LocalDateTime.now()),
                generateCandle(1040.2, 1002.4, 27, 19, 9, LocalDateTime.now().minusDays(1)),
                generateCandle(1080.1, 1040.5, 28, 14, 5, LocalDateTime.now().minusDays(2)),
                generateCandle(1100.5, 1080.6, 24, 17, 5, LocalDateTime.now().minusDays(3)),
                generateCandle(22.6, 26.9, 28, 18, 5, LocalDateTime.now().minusDays(4))
        );
        //when
        orchestration.processStocks(TICKER, FIGI, candles, null, result);
        //then
        assertAll(() -> {
            assertFalse(result.getIsDodge());
            assertTrue(result.getIsHammer());
            assertEquals(FIGI, result.getFigi());
        });
    }

    @Test
    @DisplayName("test hammer processing")
    void testHammerProcessing() {
        //given
        var result = new ProcessingResult();
        var candles = List.of(
                generateCandle(20.4, 21.2, 21.3, 17, 10, LocalDateTime.now()),
                generateCandle(21.9, 20.4, 22, 16, 9, LocalDateTime.now().minusDays(1)),
                generateCandle(22.9, 22.1, 23, 16, 5, LocalDateTime.now().minusDays(2)),
                generateCandle(23.6, 23.1, 21, 16, 5, LocalDateTime.now().minusDays(3)),
                generateCandle(23.9, 22.9, 26, 19, 5, LocalDateTime.now().minusDays(4))
        );
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
    @DisplayName("test large group - hammer")
    void testProcessing() {
        //given
        var result = new ProcessingResult();
        var candles = List.of(
                generateCandle(26.1, 27.2, 27.2, 24, 10, LocalDateTime.now()),
                generateCandle(28.1, 26.2, 27, 19, 9, LocalDateTime.now().minusDays(1)),
                generateCandle(28.9, 28.1, 23, 16, 5, LocalDateTime.now().minusDays(2)),
                generateCandle(29.6, 29.1, 21, 16, 5, LocalDateTime.now().minusDays(3)),
                generateCandle(29.9, 28.9, 26, 19, 5, LocalDateTime.now().minusDays(4)),
                generateCandle(30.1, 29.2, 31, 24, 10, LocalDateTime.now().minusDays(5)),
                generateCandle(25.2, 20.4, 27, 19, 9, LocalDateTime.now().minusDays(6)),
                generateCandle(20.1, 20.5, 28, 14, 5, LocalDateTime.now().minusDays(7)),
                generateCandle(20.5, 22.6, 24, 17, 5, LocalDateTime.now().minusDays(8)),
                generateCandle(22.6, 26.9, 28, 18, 5, LocalDateTime.now().minusDays(9))
        );
        //when
        orchestration.processStocks(TICKER, FIGI, candles, null, result);
        //then
        assertAll(() -> {
            assertFalse(result.getIsDodge());
            assertTrue(result.getIsHammer());
            assertEquals(FIGI, result.getFigi());
        });
    }

    @Test
    @DisplayName("test single candle")
    void testSingleCandleProcessing() {
        var result = new ProcessingResult();
        var c1 = generateCandle(30.1, 25.2, 31, 24, 10, LocalDateTime.now());
        orchestration.processStocks(TICKER, FIGI, List.of(c1), null, result);
        assertNull(result.getIsDodge());
        assertNull(result.getIsHammer());
    }

    @Test
    @DisplayName("test null entry")
    void testNullProcessing() {
        assertThrows(NullPointerException.class,
                () -> orchestration.processStocks(null, null, null, null, null));
    }

    @TestConfiguration
    @ComponentScan(basePackages = {"com.home.project.stocks.service"})
    static class Config {

        @Bean
        DodgeProcessor dodgeProcessor() {
            return new DodgeProcessor();
        }

        @Bean
        HammerProcessor hammerProcessor() {
            return new HammerProcessor();
        }

        @MockBean
        AlphaVantageApiClient alphaVantageApiClient;

        @MockBean
        RepositoryService repositoryService;
    }
}