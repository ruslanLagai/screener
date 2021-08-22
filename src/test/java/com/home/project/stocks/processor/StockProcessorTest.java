package com.home.project.stocks.processor;

import com.home.project.stocks.model.aplha.vantage.EmaPeriod;
import com.home.project.stocks.model.aplha.vantage.Interval;
import com.home.project.stocks.model.aplha.vantage.RsiPeriod;
import com.home.project.stocks.model.aplha.vantage.SeriesType;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.repository.*;
import com.home.project.stocks.service.IndicatorService;
import com.home.project.stocks.utils.DateTimeParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.home.project.stocks.model.indicators.ParsedIndicator.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@DisplayName("Test stock processing")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = StockProcessorTest.Config.class)
class StockProcessorTest extends AbstractRepositoryTest {

    public static final String AAPL = "aapl";
    @Autowired
    StockProcessor stockProcessor;

    @Autowired
    HammerRepository hammerRepository;

    @Autowired
    DodgeRepository dodgeRepository;

    @Autowired
    IndicatorRepository indicatorRepository;

    @Autowired
    CandleRepository candleRepository;

    @Test
    @DisplayName("Basic test")
    void processStock() {
        var candle1 = generateCandle(10, 20, 21, 9, 15);
        var candle2 = generateCandle(10, 20, 21, 9, 15);
        candle1.setFigi("figi1");
        candle2.setFigi("figi1");

        stockProcessor.processStock(AAPL, "figi1", new Candle[]{candle1, candle2});

        var dodge = dodgeRepository.getDodgeIndexByTicker(AAPL);
        var hammer = hammerRepository.getHammerIndexByTicker(AAPL);
        var indicator = indicatorRepository.getByTicker(AAPL);
        var candle = candleRepository.findCandleIndexByFigi("figi1");

        assertAll(() -> {
            assertNull(dodge);
            assertNull(hammer);
            assertEquals(AAPL, indicator.getTicker());
            assertEquals(candle.getL(), 9);
            assertEquals(candle.getId(), indicator.getCandleId());
        });
    }

    @TestConfiguration
    @ComponentScan(basePackages = "com.home.project.stocks.processor")
    static class Config {

        @Bean
        IndicatorService indicatorService() {
            var indicatorService = mock(IndicatorService.class);

            Map<Date, Double> indicatorData = new HashMap<>();
            indicatorData.put(DateTimeParser.parseDate("2021-07-22"), 15.0);
            indicatorData.put(DateTimeParser.parseDate("2021-07-21"), 14.6213);
            var emaParsedIndicator = new ParsedIndicator(indicatorData, null, AAPL, "daily");

            Map<Date, Double> rsiData = new HashMap<>();
            rsiData.put(DateTimeParser.parseDate("2021-07-22"), 25.0);
            rsiData.put(DateTimeParser.parseDate("2021-07-21"), 24.6213);
            var rsiParsedIndicator = new ParsedIndicator(rsiData, null, AAPL, "daily");

            Map<Date, Map<String, Double>> macdData = new HashMap<>();
            macdData.put(DateTimeParser.parseDate("2021-07-22"), Map.of(
                    MACD, 15.0,
                    MACD_SIGNAL, 14.0,
                    MACD_HIST, 1.0));
            macdData.put(DateTimeParser.parseDate("2021-07-21"), Map.of(
                    MACD, 16.0,
                    MACD_SIGNAL, 15.0,
                    MACD_HIST, 2.0));
            var macdParsedIndicator = new ParsedIndicator(null, macdData, AAPL, "daily");

            doReturn(emaParsedIndicator).when(indicatorService)
                    .getEma(AAPL, Interval.ONE_DAY, EmaPeriod.TWO_HUNDRED, SeriesType.CLOSE);
            doReturn(emaParsedIndicator).when(indicatorService)
                    .getEma(AAPL, Interval.ONE_DAY, EmaPeriod.ONE_THOUSAND, SeriesType.CLOSE);
            doReturn(rsiParsedIndicator).when(indicatorService)
                    .getRsi(AAPL, Interval.ONE_DAY, RsiPeriod.NINE, SeriesType.CLOSE);
            doReturn(macdParsedIndicator).when(indicatorService)
                    .getMacd(AAPL, Interval.ONE_DAY, SeriesType.CLOSE);

            return indicatorService;
        }
    }
}