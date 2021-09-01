package com.home.project.stocks.processor;

import java.time.Instant;
import java.time.Period;
import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import org.elasticsearch.common.collect.Map;
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
import com.home.project.stocks.model.aplha.vantage.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.service.NotifierService;
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
        c1.setClose(1.0);
        c1.setHigh(2.0);
        c1.setLow(4.0);
        c1.setOpen(5.0);
        c1.setVolume(6.0);
        var result = new ProcessingResult();
        orchestration.processStocks("testTicker", "testFigi",
                Map.of(Date.from(Instant.now()), c1), null, result);
        assertFalse(result.getIsDodge());
        assertFalse(result.getIsHammer());
    }

    @Test
    @DisplayName("test dodge processing")
    void testDodgeProcessing() {
        //given
        var result = new ProcessingResult();
        var candles = Map.of(
                Date.from(Instant.now()), generateCandle(30.1, 25.2, 31, 24, 10),
                Date.from(Instant.now().minus(Period.ofDays(1))), generateCandle(25.2, 20.4, 27, 19, 9),
                Date.from(Instant.now().minus(Period.ofDays(2))), generateCandle(20.1, 20.5, 28, 14, 5),
                Date.from(Instant.now().minus(Period.ofDays(3))), generateCandle(20.5, 22.6, 24, 17, 5),
                Date.from(Instant.now().minus(Period.ofDays(4))), generateCandle(22.6, 26.9, 28, 18, 5)
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
    @DisplayName("test hammer processing")
    void testHammerProcessing() {
        //given
        var result = new ProcessingResult();
        var candles = Map.of(
                Date.from(Instant.now()), generateCandle(20.4, 21.2, 22, 17, 10),
                Date.from(Instant.now().minus(Period.ofDays(1))), generateCandle(19.2, 20.4, 22, 16, 9),
                Date.from(Instant.now().minus(Period.ofDays(2))), generateCandle(21.1, 19.6, 23, 16, 5),
                Date.from(Instant.now().minus(Period.ofDays(3))), generateCandle(22.6, 21.9, 21, 16, 5),
                Date.from(Instant.now().minus(Period.ofDays(4))), generateCandle(23.9, 22.9, 26, 19, 5)
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
        var candles = Map.of(
                Date.from(Instant.now()), generateCandle(26.1, 27.2, 31, 24, 10),
                Date.from(Instant.now().minus(Period.ofDays(1))), generateCandle(25.2, 26.7, 27, 19, 9),
                Date.from(Instant.now().minus(Period.ofDays(2))), generateCandle(27.1, 25.6, 23, 16, 5),
                Date.from(Instant.now().minus(Period.ofDays(3))), generateCandle(28.6, 27.9, 21, 16, 5),
                Date.from(Instant.now().minus(Period.ofDays(4))), generateCandle(29.9, 28.9, 26, 19, 5),
                Date.from(Instant.now().minus(Period.ofDays(5))), generateCandle(30.1, 29.2, 31, 24, 10),
                Date.from(Instant.now().minus(Period.ofDays(6))), generateCandle(25.2, 20.4, 27, 19, 9),
                Date.from(Instant.now().minus(Period.ofDays(7))), generateCandle(20.1, 20.5, 28, 14, 5),
                Date.from(Instant.now().minus(Period.ofDays(8))), generateCandle(20.5, 22.6, 24, 17, 5),
                Date.from(Instant.now().minus(Period.ofDays(9))), generateCandle(22.6, 26.9, 28, 18, 5)
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
        var c1 = generateCandle(30.1, 25.2, 31, 24, 10);
        orchestration.processStocks(TICKER, FIGI, Map.of(Date.from(Instant.now()), c1), null, result);
        assertFalse(result.getIsDodge());
        assertFalse(result.getIsHammer());
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
        };

        @MockBean
        AlphaVantageApiClient alphaVantageApiClient;

        @MockBean
        NotifierService notifierService;

        @MockBean
        RepositoryService repositoryService;
    }
}