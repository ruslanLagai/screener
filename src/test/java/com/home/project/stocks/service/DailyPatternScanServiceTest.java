package com.home.project.stocks.service;

import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

import com.home.project.stocks.client.TwelvedataApiClient;
import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.processor.DodgeProcessor;
import com.home.project.stocks.processor.HammerProcessor;
import com.home.project.stocks.repository.AbstractRepositoryTest;
import com.home.project.stocks.repository.CandleRepository;
import com.home.project.stocks.service.impl.DailyPatternScanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Class to test {@link DailyPatternScanService}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DailyPatternScanServiceTest.Config.class, AbstractRepositoryTest.Config.class},
        initializers = AbstractRepositoryTest.Config.class)
class DailyPatternScanServiceTest extends AbstractRepositoryTest {

    static {
        container.start();
    }

    @Autowired
    private DailyPatternScanService dailyPatternScanService;

    @Autowired
    private CandleRepository candleRepository;

    @Test
    @DisplayName("basic test")
    void processStocks() {
        dailyPatternScanService.processStock("AAPL", null);
        var candle = candleRepository.findByTickerAndTimeAfter("AAPL", LocalDateTime.now().minusDays(3));
        if (candle == null) {
            assertTrue(candleRepository.findAll().isEmpty());
        } else {
            assertNotEquals(0.0, candle.getClose());
            assertNotEquals(0.0, candle.getOpen());
            assertNotEquals(0.0, candle.getHigh());
            assertNotEquals(0.0, candle.getLow());
            assertNotEquals(0.0, candle.getVolume());
            assertEquals(Interval.TWELVE_DATA_ONE_DAY.getInterval(), candle.getInterval());
        }
    }

    @Test
    @DisplayName("test invalid ticker")
    void testInvalidProcessing() {
        dailyPatternScanService.processStock("AAadfPL", null);
        assertTrue(candleRepository.findAll().isEmpty());
    }

    @TestConfiguration
    @ComponentScan(basePackages = {"com.home.project.stocks.service"})
    @EnableFeignClients(clients = TwelvedataApiClient.class)
    @Import({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
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