package com.home.project.stocks.service;

import com.home.project.stocks.client.TwelvedataApiClient;
import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.processor.EngulfingProcessor;
import com.home.project.stocks.processor.HammerProcessor;
import com.home.project.stocks.repository.AbstractRepositoryTest;
import com.home.project.stocks.repository.CandleRepository;
import com.home.project.stocks.service.impl.DailyPatternScanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Class to test {@link DailyPatternScanService}
 */
@SpringJUnitConfig(classes = {DailyPatternScanServiceTest.Config.class, AbstractRepositoryTest.Config.class},
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
        dailyPatternScanService.processStock("BIO", null);
        var candle = candleRepository.findByTickerAndTimeAfter("BIO", LocalDateTime.now().minusDays(3));
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
        assertNull(candleRepository.findByTickerAndTimeAfter("AAadfPL", LocalDate.now().atTime(LocalTime.MIN)));
    }

    @TestConfiguration
    @ComponentScan(basePackages = {"com.home.project.stocks.service", "com.home.project.stocks.mapper"})
    @EnableFeignClients(clients = TwelvedataApiClient.class)
    @Import({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
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