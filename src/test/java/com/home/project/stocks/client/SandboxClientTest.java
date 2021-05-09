package com.home.project.stocks.client;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.home.project.stocks.config.RestConfig;
import com.home.project.stocks.exceptions.TinkoffServerException;
import com.home.project.stocks.model.Currency;
import com.home.project.stocks.model.candles.Interval;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:sandbox.properties")
@ContextConfiguration(classes = {RestConfig.class, SandboxClientTest.TestConfig.class})
class SandboxClientTest {

    @Autowired
    private SandboxClient sandboxClient;

    @Test
    void getStocks() {
        var response = sandboxClient.getStocks();
        var body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatus(), Matchers.equalToIgnoringCase("ok"));
        assertNotNull(body.getTrackingId());
        assertThat(body.getPayload().getInstruments().length, Matchers.equalTo(body.getPayload().getTotal()));
    }

    @Test
    void getStockByTicker() {
        var response = sandboxClient.getStockByTicker("AAPL");
        var body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatus(), Matchers.equalToIgnoringCase("ok"));
        assertNotNull(body.getTrackingId());
        assertThat(body.getPayload().getInstruments().length, Matchers.equalTo(1));
        assertEquals(body.getPayload().getInstruments()[0].getCurrency(), Currency.USD);
        assertEquals(body.getPayload().getInstruments()[0].getName(), "Apple");
        assertEquals(body.getPayload().getInstruments()[0].getLot(), 1);
        assertEquals(body.getPayload().getInstruments()[0].getFigi(), "BBG000B9XRY4");
    }

    @Test
    @DisplayName("Test not correct ticker")
    void getStockByTickerNotFound() {
        var response = sandboxClient.getStockByTicker("APPL");
        var body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatus(), Matchers.equalToIgnoringCase("ok"));
        assertNotNull(body.getTrackingId());
        assertThat(body.getPayload().getInstruments().length, Matchers.equalTo(0));
    }

    @Test
    @DisplayName("Test get candles")
    void getCandles() {
        var from = new DateTime(2021, 4, 14, 7, 0, 0);
        var to = new DateTime(2021, 4, 14, 12, 0, 0);

        var response = sandboxClient.getCandles("BBG000B9XRY4", from, to, Interval.ONE_HOUR);
        var body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatus(), Matchers.equalToIgnoringCase("ok"));
        assertThat(body.getPayload().getCandles().length, Matchers.equalTo(5));

        Stream.of(body.getPayload().getCandles()).forEach(candle ->
            assertAll(() -> {
                assertThat(candle.getInterval(), Matchers.equalTo(Interval.ONE_HOUR.getPeriod()));
                assertThat(candle.getFigi(), Matchers.equalTo("BBG000B9XRY4"));
                assertNotEquals(candle.getL(), 0);
                assertNotEquals(candle.getC(), 0);
                assertNotEquals(candle.getH(), 0);
                assertNotEquals(candle.getO(), 0);
                assertNotEquals(candle.getV(), 0);
                assertNotNull(candle.getTime());
        }));
    }

    @Test
    @DisplayName("Test get candles - incorrect interval")
    void getCandlesError() {
        var from = new DateTime(2021, 4, 14, 7, 0, 0);
        var to = new DateTime(2021, 4, 14, 7, 0, 0);

        assertThrows(TinkoffServerException.class,
                () -> sandboxClient.getCandles("BBG000B9XRY4", from, to, Interval.ONE_HOUR));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public SandboxClient sandboxClient() {
            return new SandboxClient();
        }
    }
}