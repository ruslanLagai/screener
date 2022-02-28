package com.home.project.stocks.processor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.home.project.stocks.model.aplha.vantage.EmaPeriod;
import com.home.project.stocks.model.aplha.vantage.Interval;
import com.home.project.stocks.model.aplha.vantage.RsiPeriod;
import com.home.project.stocks.model.aplha.vantage.SeriesType;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.entity.DailyEma;
import com.home.project.stocks.model.entity.DailyMacd;
import com.home.project.stocks.model.entity.DailyRsi;
import com.home.project.stocks.service.impl.IndicatorOrchestration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.home.project.stocks.exceptions.ProcessingException;
import com.home.project.stocks.helpers.YamlPropertySourceFactory;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.service.IndicatorService;

/**
 * Class to test {@link IndicatorOrchestration}
 */
@Disabled
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = IndicatorOrchestrationTest.Config.class)
class IndicatorOrchestrationTest extends AbstractProcessorTest {

    private static final String TICKER = "AAPL";
    private final Candle candle = generateCandle(10.0, 15.0, 17.0, 9.0, 10, LocalDateTime.now());

    @Autowired
    IndicatorOrchestration orchestration;

    @Test
    @DisplayName("basic test")
    void processStock() {
        var result = new ProcessingResult();
        orchestration.processStocks("AAPL", "", List.of(candle), candle, result);
        assertAll(() -> {
            assertEquals(candle.getC(), result.getClosePrice());
            assertEquals(candle.getO(), result.getOpenPrice());
            assertEquals(candle.getH(), result.getMaxPrice());
            assertEquals(candle.getL(), result.getMinPrice());
            assertEquals(candle.getV(), result.getVolume());
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
    @PropertySource(value = "classpath:application-test.yml", factory = YamlPropertySourceFactory.class)
    static class Config {

        @Bean
        IndicatorService indicatorService() {
            var indicatorService = mock(IndicatorService.class);

            List<DailyEma> indicatorData = List.of(
                    DailyEma.builder()
                            .emaValue(15.0)
                            .datetime(LocalDate.parse("2021-07-22").atTime(23, 59))
                            .build(),
                    DailyEma.builder()
                            .emaValue(14.6213)
                            .datetime(LocalDate.parse("2021-07-22").atTime(23, 59))
                            .build(),
                    DailyEma.builder()
                            .emaValue(14.9468)
                            .datetime(LocalDate.parse("2021-07-22").atTime(23, 59))
                            .build()
            );
            var emaParsedIndicator = ParsedIndicator.builder()
                    .ticker(TICKER)
                    .ema(indicatorData)
                    .build();

            List<DailyRsi> rsiData = List.of(
                    DailyRsi.builder()
                            .rsiValue(25.0)
                            .datetime(LocalDate.parse("2021-07-22").atTime(23, 59))
                            .build(),
                    DailyRsi.builder()
                            .rsiValue(24.6213)
                            .datetime(LocalDate.parse("2021-07-22").atTime(23, 59))
                            .build(),
                    DailyRsi.builder()
                            .rsiValue(24.9468)
                            .datetime(LocalDate.parse("2021-07-22").atTime(23, 59))
                            .build()
            );
            var rsiParsedIndicator = ParsedIndicator.builder()
                    .rsi(rsiData)
                    .ticker(TICKER)
                    .build();

            List<DailyMacd> macdData = List.of(
                    DailyMacd.builder()
                            .macdValue(15.0)
                            .macdSignalValue(14.0)
                            .macdHistValue(1.0)
                            .datetime(LocalDate.parse("2021-07-22").atTime(23, 59))
                            .build(),
                    DailyMacd.builder()
                            .macdValue(16.0)
                            .macdSignalValue(15.0)
                            .macdHistValue(2.0)
                            .datetime(LocalDate.parse("2021-07-22").atTime(23, 59))
                            .build(),
                    DailyMacd.builder()
                            .macdValue(16.0)
                            .macdSignalValue(16.0)
                            .macdHistValue(3.0)
                            .datetime(LocalDate.parse("2021-07-22").atTime(23, 59))
                            .build()
            );
            var macdParsedIndicator = ParsedIndicator.builder()
                    .ticker(TICKER)
                    .interval("1day")
                    .macd(macdData)
                    .build();

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