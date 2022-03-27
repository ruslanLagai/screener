package com.home.project.stocks.service;

import com.home.project.stocks.client.TwelvedataApiClient;
import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.processor.DodgeProcessor;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Class to test {@link DailyPatternScanService}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DailyPatternScanServiceMockTest.Config.class, AbstractRepositoryTest.Config.class},
        initializers = AbstractRepositoryTest.Config.class)
class DailyPatternScanServiceMockTest extends AbstractRepositoryTest {

    private static final String AAPL_DODGE = "AAPL";
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
        when(twelvedataApiClient.getCandles(eq(AAPL_DODGE), eq(Interval.TWELVE_DATA_ONE_DAY.getInterval())))
                .thenReturn(readCandles("templates/candles/dodge.json"));
        when(twelvedataApiClient.getCandles(eq(FB_HAMMER), eq(Interval.TWELVE_DATA_ONE_DAY.getInterval())))
                .thenReturn(readCandles("templates/candles/hammer.json"));
        when(twelvedataApiClient.getCandles(eq(AMZN), eq(Interval.TWELVE_DATA_ONE_DAY.getInterval())))
                .thenReturn(readCandles("templates/candles/candles.json"));
    }

    @Test
    @DisplayName("dodge test")
    void processStocks() throws InterruptedException {
        dailyPatternScanService.processStock(AAPL_DODGE, null);
        Thread.sleep(5000);
        var saved = candleRepository.findByTickerAndTimeAfter(AAPL_DODGE, LocalDateTime.now().minusYears(3));

        assertAll(() -> {
            assertEquals(169.82001, saved.getOpen());
            assertEquals(175.53999, saved.getHigh());
            assertEquals(164.19000, saved.getLow());
            assertEquals(169.30000, saved.getClose());
            assertTrue(saved.isDodge());
            assertFalse(saved.isHammer());
            assertEquals(Interval.TWELVE_DATA_ONE_DAY.getInterval(), saved.getInterval());
            assertEquals(AAPL_DODGE, saved.getTicker());
            assertNull(saved.getFigi());
        });
    }

    @Test
    @DisplayName("hammer test")
    void testHammer() throws InterruptedException {
        dailyPatternScanService.processStock(FB_HAMMER, null);
        Thread.sleep(4000);
        var saved = candleRepository.findByTickerAndTimeAfter(FB_HAMMER, LocalDateTime.now().minusYears(3));

        assertAll(() -> {
            assertEquals(209.39000, saved.getOpen());
            assertEquals(210.00000, saved.getHigh());
            assertEquals(200.17999, saved.getLow());
            assertEquals(206.17999, saved.getClose());
            assertTrue(saved.isHammer());
            assertFalse(saved.isDodge());
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
        DodgeProcessor dodgeProcessor() {
            return new DodgeProcessor();
        }

        @Bean
        HammerProcessor hammerProcessor() {
            return new HammerProcessor();
        }
    }
}