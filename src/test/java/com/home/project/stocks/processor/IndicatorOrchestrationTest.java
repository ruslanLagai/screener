package com.home.project.stocks.processor;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.home.project.stocks.model.indicators.ParsedIndicator.*;

import com.home.project.stocks.exceptions.ProcessingException;
import com.home.project.stocks.helpers.YamlPropertySourceFactory;
import com.home.project.stocks.model.aplha.vantage.*;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.service.AlphaVantageService;
import com.home.project.stocks.service.RepositoryService;
import com.home.project.stocks.utils.DateTimeParser;

/**
 * Class to test {@link IndicatorOrchestration}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = IndicatorOrchestrationTest.Config.class)
class IndicatorOrchestrationTest extends AbstractProcessorTest {

    private static final String TICKER = "ticker";
    private final Candle candle = generateCandle(10.0, 15.0, 17.0, 9.0, 10);

    @Autowired
    IndicatorOrchestration orchestration;

    @Test
    @DisplayName("basic test")
    void processStock() {
        var result = new ProcessingResult();
        var date = Date.from(Instant.now());
        orchestration.processStocks(TICKER, "", Map.of(date, candle), date, result);
        assertAll(() -> {
            assertEquals(candle.getClose(), result.getClosePrice());
            assertEquals(candle.getOpen(), result.getOpenPrice());
            assertEquals(candle.getHigh(), result.getMaxPrice());
            assertEquals(candle.getLow(), result.getMinPrice());
            assertEquals(candle.getVolume(), result.getVolume());
            assertEquals(ProcessingResult.RsiSign.OVERSOLD, result.getRsiSign());
            assertEquals(ProcessingResult.Trend.NO_SIGN, result.getMacdSignalTrend());
            assertEquals(ProcessingResult.Trend.DESCENDING, result.getMacdBarTrend());
            assertThat(result.getMacdBarValues(), Matchers.contains(1.0, 2.0, 3.0));
        });
    }

    @Test
    @DisplayName("test candle is null")
    void processStockCandleNull() {
        assertThrows(NullPointerException.class,
                () -> orchestration.processStocks(TICKER, "", null, null, new ProcessingResult()));
    }

    @Test
    @DisplayName("test ticker is null")
    void processStockTickerNull() {
        assertThrows(ProcessingException.class,
                () -> orchestration.processStocks("", "", null, null, new ProcessingResult()));
    }

    /**
     * Config class
     */
    @TestConfiguration
    @ComponentScan(basePackages = {"com.home.project.stocks.processor"},
            excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                    value = {PatternOrchestrationTest.Config.class, StockProcessorTest.Config.class})})
    @PropertySource(value = "classpath:application-test.yml", factory = YamlPropertySourceFactory.class)
    static class Config {

        @MockBean
        RepositoryService repositoryService;

        @Bean
        AlphaVantageService indicatorService() {
            var indicatorService = mock(AlphaVantageService.class);

            Map<Date, Double> indicatorData = new HashMap<>();
            indicatorData.put(DateTimeParser.parseDate("2021-07-22"), 15.0);
            indicatorData.put(DateTimeParser.parseDate("2021-07-21"), 14.6213);
            indicatorData.put(DateTimeParser.parseDate("2021-07-20"), 14.9468);
            var emaParsedIndicator = new ParsedIndicator(indicatorData, null, TICKER, "daily");

            Map<Date, Double> rsiData = new HashMap<>();
            rsiData.put(DateTimeParser.parseDate("2021-07-22"), 25.0);
            rsiData.put(DateTimeParser.parseDate("2021-07-21"), 24.6213);
            rsiData.put(DateTimeParser.parseDate("2021-07-20"), 24.9468);
            var rsiParsedIndicator = new ParsedIndicator(rsiData, null, TICKER, "daily");

            Map<Date, Map<String, Double>> macdData = new HashMap<>();
            macdData.put(DateTimeParser.parseDate("2021-07-22"), Map.of(
                    MACD, 15.0,
                    MACD_SIGNAL, 14.0,
                    MACD_HIST, 1.0));
            macdData.put(DateTimeParser.parseDate("2021-07-21"), Map.of(
                    MACD, 16.0,
                    MACD_SIGNAL, 15.0,
                    MACD_HIST, 2.0));
            macdData.put(DateTimeParser.parseDate("2021-07-20"), Map.of(
                    MACD, 16.0,
                    MACD_SIGNAL, 15.0,
                    MACD_HIST, 3.0));
            var macdParsedIndicator = new ParsedIndicator(null, macdData, TICKER, "daily");

            doReturn(emaParsedIndicator).when(indicatorService)
                    .getEma(TICKER, Interval.ONE_DAY, EmaPeriod.TWO_HUNDRED, SeriesType.CLOSE);
            doReturn(emaParsedIndicator).when(indicatorService)
                    .getEma(TICKER, Interval.ONE_DAY, EmaPeriod.ONE_THOUSAND, SeriesType.CLOSE);
            doReturn(rsiParsedIndicator).when(indicatorService)
                    .getRsi(TICKER, Interval.ONE_DAY, RsiPeriod.NINE, SeriesType.CLOSE);
            doReturn(macdParsedIndicator).when(indicatorService)
                    .getMacd(TICKER, Interval.ONE_DAY, SeriesType.CLOSE);

            return indicatorService;
        }
    }
}