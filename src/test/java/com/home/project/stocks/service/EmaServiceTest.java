package com.home.project.stocks.service;

import com.home.project.stocks.client.AlphaVantageApiClient;
import com.home.project.stocks.config.FeingConfig;
import com.home.project.stocks.model.aplha.vantage.EmaPeriod;
import com.home.project.stocks.model.aplha.vantage.Interval;
import com.home.project.stocks.model.aplha.vantage.SeriesType;
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
@ContextConfiguration(classes = {EmaServiceTest.Config.class })
@Import({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
@EnableConfigurationProperties
@TestPropertySource("classpath:sandbox.properties")
class EmaServiceTest {

    private static final String TICKER = "IBM";

    @Autowired
    private EmaService emaService;

    @Test
    @DisplayName("Basic test")
    void getEma() {
        var ema = emaService.getEma(TICKER, Interval.ONE_DAY, EmaPeriod.ONE_HUNDRED, SeriesType.OPEN);
        assertFalse(ema.getEma().isEmpty());
    }

    @DisplayName("Parametrized for intervals")
    @ParameterizedTest
    @EnumSource(value = Interval.class, names = {"ONE_HOUR", "ONE_WEEK"})
    void testParametrizedInterval(Interval interval) {
        var ema = emaService.getEma(TICKER, interval, EmaPeriod.ONE_HUNDRED, SeriesType.OPEN);
        assertFalse(ema.getEma().isEmpty());
    }

    @TestConfiguration
    @EnableFeignClients(clients = AlphaVantageApiClient.class)
    static class Config {

        @Bean
        EmaService emaService() {
            return new EmaService();
        }

        @Bean
        FeingConfig feingConfig() {
            return new FeingConfig();
        }
    }
}