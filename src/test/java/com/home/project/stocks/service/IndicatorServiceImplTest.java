package com.home.project.stocks.service;

import com.home.project.stocks.client.AlphaVantageApiClient;
import com.home.project.stocks.config.FeingConfig;
import com.home.project.stocks.model.aplha.vantage.EmaPeriod;
import com.home.project.stocks.model.aplha.vantage.Interval;
import com.home.project.stocks.model.aplha.vantage.RsiPeriod;
import com.home.project.stocks.model.aplha.vantage.SeriesType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("Test Ema service")
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {IndicatorServiceImplTest.Config.class })
@Import({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
@EnableConfigurationProperties
@TestPropertySource("classpath:sandbox.properties")
class IndicatorServiceImplTest {

    private static final String TICKER = "IBM";

    @BeforeEach
    public void initialise() throws InterruptedException {
        Thread.sleep(30000);
    }

    @Autowired
    private IndicatorService indicatorServiceImpl;

    @Test
    @DisplayName("Ema - Basic test")
    void getEma() {
        var ema = indicatorServiceImpl.getEma(TICKER, Interval.ONE_DAY, EmaPeriod.ONE_HUNDRED, SeriesType.OPEN);
        assertFalse(ema.getIndicatorData().isEmpty());
    }

    @DisplayName("Ema - Parametrized for intervals")
    @ParameterizedTest
    @EnumSource(value = Interval.class, names = {"ONE_HOUR", "ONE_WEEK"})
    void testParametrizedInterval(Interval interval) {
        var ema = indicatorServiceImpl.getEma(TICKER, interval, EmaPeriod.ONE_HUNDRED, SeriesType.OPEN);
        assertFalse(ema.getIndicatorData().isEmpty());
    }

    @Test
    @DisplayName("Rsi - Basic test")
    void getRsi() {
        var ema = indicatorServiceImpl.getRsi(TICKER, Interval.ONE_DAY, RsiPeriod.NINE, SeriesType.CLOSE);
        assertFalse(ema.getIndicatorData().isEmpty());
    }

    @DisplayName("Rsi - Parametrized for Rsi period")
    @ParameterizedTest
    @EnumSource(value = RsiPeriod.class, names = {"FOURTEEN", "TWENTY_FOUR"})
    void testRsiParametrizedInterval(RsiPeriod rsiPeriod) {
        var ema = indicatorServiceImpl.getRsi(TICKER, Interval.ONE_DAY, rsiPeriod, SeriesType.OPEN);
        assertFalse(ema.getIndicatorData().isEmpty());
    }

    @Test
    @DisplayName("Macd - Basic test")
    void getMacd() {
        var ema = indicatorServiceImpl.getMacd(TICKER, Interval.ONE_DAY, SeriesType.CLOSE);
        assertFalse(ema.getMacdData().isEmpty());
    }

    @DisplayName("Macd - Parametrized for intervals")
    @ParameterizedTest
    @EnumSource(value = Interval.class, names = {"ONE_HOUR", "ONE_WEEK"})
    void testRsiParametrizedInterval(Interval interval) {
        var ema = indicatorServiceImpl.getMacd(TICKER, interval, SeriesType.OPEN);
        assertFalse(ema.getMacdData().isEmpty());
    }

    @TestConfiguration
    @EnableFeignClients(clients = AlphaVantageApiClient.class)
    static class Config {

        @Bean
        IndicatorService emaService() {
            return new IndicatorServiceImpl();
        }

        @Bean
        FeingConfig feingConfig() {
            return new FeingConfig();
        }
    }
}