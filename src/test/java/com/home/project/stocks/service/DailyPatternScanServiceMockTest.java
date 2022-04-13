package com.home.project.stocks.service;

import com.home.project.stocks.client.TwelvedataApiClient;
import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.processor.EngulfingProcessor;
import com.home.project.stocks.processor.HammerProcessor;
import com.home.project.stocks.repository.AbstractRepositoryTest;
import com.home.project.stocks.repository.CandleRepository;
import com.home.project.stocks.service.impl.DailyPatternScanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static com.home.project.stocks.utils.TestUtils.readCandles;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Class to test {@link DailyPatternScanService}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DailyPatternScanServiceMockTest.Config.class, AbstractRepositoryTest.Config.class},
        initializers = AbstractRepositoryTest.Config.class)
class DailyPatternScanServiceMockTest extends AbstractRepositoryTest {

    private static final String AAPL_ENGULFING = "AAPL";
    private static final String FB_HAMMER = "FB";
    private static final String AMZN = "AMZN";

    static {
        container.start();
    }

    @Autowired
    private DailyPatternScanService dailyPatternScanService;

    @Autowired
    private CandleRepository candleRepository;

    @MockBean
    private TwelvedataApiClient twelvedataApiClient;

    @BeforeEach
    public void init() {
        when(twelvedataApiClient.getCandles(eq(AAPL_ENGULFING), eq(Interval.TWELVE_DATA_ONE_DAY.getInterval()), anyInt()))
                .thenReturn(readCandles("templates/candles/engulfing.json"));
        when(twelvedataApiClient.getCandles(eq(FB_HAMMER), eq(Interval.TWELVE_DATA_ONE_DAY.getInterval()), anyInt()))
                .thenReturn(readCandles("templates/candles/hammer.json"));
        when(twelvedataApiClient.getCandles(eq(AMZN), eq(Interval.TWELVE_DATA_ONE_DAY.getInterval()), anyInt()))
                .thenReturn(readCandles("templates/candles/candles.json"));
    }

    @Test
    @DisplayName("engulfing pattern test")
    void processStocks() throws InterruptedException {
        dailyPatternScanService.processStock(AAPL_ENGULFING, null);
        Thread.sleep(10000);
        var saved = candleRepository.findByTickerAndTimeAfter(AAPL_ENGULFING, LocalDateTime.now().minusYears(3));

        assertAll(() -> {
            assertEquals(168.88, saved.getOpen());
            assertEquals(175.54, saved.getHigh());
            assertEquals(164.19, saved.getLow());
            assertEquals(172.30, saved.getClose());
            assertTrue(saved.isEngulfing());
            assertFalse(saved.isHammer());
            assertEquals(Interval.TWELVE_DATA_ONE_DAY.getInterval(), saved.getInterval());
            assertEquals(AAPL_ENGULFING, saved.getTicker());
            assertNull(saved.getFigi());
        });
    }

    @Test
    @DisplayName("hammer test")
    void testHammer() throws InterruptedException {
        dailyPatternScanService.processStock(FB_HAMMER, null);
        Thread.sleep(10000);
        var saved = candleRepository.findByTickerAndTimeAfter(FB_HAMMER, LocalDateTime.now().minusYears(3));

        assertAll(() -> {
            assertEquals(209.39, saved.getOpen());
            assertEquals(210.0, saved.getHigh());
            assertEquals(200.18, saved.getLow());
            assertEquals(206.18, saved.getClose());
            assertTrue(saved.isHammer());
            assertFalse(saved.isEngulfing());
            assertEquals(Interval.TWELVE_DATA_ONE_DAY.getInterval(), saved.getInterval());
            assertEquals(FB_HAMMER, saved.getTicker());
            assertNull(saved.getFigi());
        });
    }

    @Test
    @DisplayName("no pattern test")
    void testNoPattern() {
        dailyPatternScanService.processStock(AMZN, null);
        var saved = candleRepository.findByTickerAndTimeAfter(AMZN, LocalDateTime.now().minusYears(3));

        assertNull(saved);
    }

    @TestConfiguration
    @ComponentScan(basePackages = {"com.home.project.stocks.service"})
    static class Config {

        @Bean
        EngulfingProcessor engulfingProcessor() {
            return new EngulfingProcessor();
        }

        @Bean
        HammerProcessor hammerProcessor() {
            return new HammerProcessor();
        }
    }
}