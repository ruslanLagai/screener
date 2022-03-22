package com.home.project.stocks.service;

import com.home.project.stocks.client.TwelvedataApiClient;
import com.home.project.stocks.helpers.YamlPropertySourceFactory;
import com.home.project.stocks.model.aplha.vantage.EmaPeriod;
import com.home.project.stocks.model.aplha.vantage.Interval;
import com.home.project.stocks.model.aplha.vantage.RsiPeriod;
import com.home.project.stocks.model.aplha.vantage.SeriesType;
import com.home.project.stocks.repository.AbstractRepositoryTest;
import com.home.project.stocks.repository.CandleRepository;
import com.home.project.stocks.repository.ChatRepository;
import com.home.project.stocks.repository.DailyEmaRepository;
import com.home.project.stocks.repository.DailyIndicatorDataRepository;
import com.home.project.stocks.repository.DailyRsiRepository;
import com.home.project.stocks.service.impl.DailyIndicatorService;
import com.home.project.stocks.service.impl.DbUpdateServiceImpl;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * class to test {@link DailyIndicatorService}
 *
 * @author rlagay
 */
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@ContextConfiguration(classes = {DailyIndicatorServiceTest.Config.class, AbstractRepositoryTest.Config.class},
        initializers = AbstractRepositoryTest.Config.class)
class DailyIndicatorServiceTest extends AbstractRepositoryTest {

    private static final String AAPL = "AAPL";

    static {
        container.start();
    }

    @Autowired
    IndicatorService dailyIndicatorService;

    @Autowired
    DailyIndicatorDataRepository indicatorDataRepository;

    @Test
    @DisplayName("Test get ema")
    void a1GetEma() throws InterruptedException {
        var ema = dailyIndicatorService.getEma(AAPL, Interval.TWELVE_DATA_ONE_DAY, EmaPeriod.TWO_HUNDRED, SeriesType.CLOSE);
        Thread.sleep(3000);
        var saved = indicatorDataRepository.findByTicker(AAPL);
        assertAll(() -> {
            assertEquals(Interval.TWELVE_DATA_ONE_DAY.getInterval(), ema.getInterval());
            assertEquals(AAPL, ema.getTicker());
            ema.getEma().forEach(indicator -> {
                assertNotEquals(0.0, indicator.getEmaValue());
                assertEquals(EmaPeriod.TWO_HUNDRED.getPeriod(), indicator.getEmaType());
                assertNotEquals(null, indicator.getDatetime());
            });
        });

        assertAll(() -> {
            assertNotNull(saved);
            assertEquals(Interval.TWELVE_DATA_ONE_DAY.getInterval(), saved.getTimeframe());
            assertEquals(AAPL, saved.getTicker());
            assertEquals(5, saved.getEmaData().size());
            saved.getEmaData().forEach(indicator -> {
                assertNotEquals(0.0, indicator.getEmaValue());
                assertEquals(EmaPeriod.TWO_HUNDRED.getPeriod(), indicator.getEmaType());
                assertNotEquals(null, indicator.getDatetime());
            });
        });
    }

    @Test
    @DisplayName("Test get rsi")
    void a2GetRsi() throws InterruptedException {
        var rsi = dailyIndicatorService.getRsi(AAPL, Interval.TWELVE_DATA_ONE_DAY, RsiPeriod.NINE, SeriesType.CLOSE);
        Thread.sleep(3000);
        var saved = indicatorDataRepository.findByTicker(AAPL);
        assertEquals(AAPL, rsi.getTicker());
        assertEquals(Interval.TWELVE_DATA_ONE_DAY.getInterval(), rsi.getInterval());
        rsi.getRsi().forEach(indicator -> {
            assertNotEquals(0.0, indicator.getRsiValue());
            assertNotEquals(null, indicator.getDatetime());
        });

        assertAll(() -> {
            assertNotNull(saved);
            assertEquals(Interval.TWELVE_DATA_ONE_DAY.getInterval(), saved.getTimeframe());
            assertEquals(AAPL, saved.getTicker());
            assertEquals(5, saved.getEmaData().size());
            saved.getEmaData().forEach(indicator -> {
                assertNotEquals(0.0, indicator.getEmaValue());
                assertEquals(EmaPeriod.TWO_HUNDRED.getPeriod(), indicator.getEmaType());
                assertNotEquals(null, indicator.getDatetime());
            });
            assertEquals(5, saved.getRsiData().size());
            saved.getRsiData().forEach(indicator -> {
                assertNotEquals(0.0, indicator.getRsiValue());
                assertNotEquals(null, indicator.getDatetime());
            });
        });
    }

//    @Test
    @DisplayName("Test get macd")
    void a3GetMacd() throws InterruptedException {
        var macd = dailyIndicatorService.getMacd(AAPL, Interval.TWELVE_DATA_ONE_DAY, SeriesType.CLOSE);
        Thread.sleep(3000);

        var saved = indicatorDataRepository.findByTicker(AAPL);
        assertEquals(AAPL, macd.getTicker());
        assertEquals(Interval.TWELVE_DATA_ONE_DAY.getInterval(), macd.getInterval());
        macd.getMacd().forEach(indicator -> {
            assertNotEquals(0.0, indicator.getMacdHistValue());
            assertNotEquals(0.0, indicator.getMacdSignalValue());
            assertNotEquals(0.0, indicator.getMacdValue());
            assertNotEquals(null, indicator.getDatetime());
        });
        assertAll(() -> {
            assertNotNull(saved);
            assertEquals(Interval.TWELVE_DATA_ONE_DAY.getInterval(), saved.getTimeframe());
            assertEquals(AAPL, saved.getTicker());
            assertEquals(5, saved.getEmaData().size());
            saved.getEmaData().forEach(indicator -> {
                assertNotEquals(0.0, indicator.getEmaValue());
                assertEquals(EmaPeriod.TWO_HUNDRED.getPeriod(), indicator.getEmaType());
                assertNotEquals(null, indicator.getDatetime());
            });
            saved.getRsiData().forEach(indicator -> {
                assertNotEquals(0.0, indicator.getRsiValue());
                assertNotEquals(null, indicator.getDatetime());
            });
            saved.getMacdData().forEach(indicator -> {
                assertNotEquals(0.0, indicator.getMacdValue());
                assertNotEquals(0.0, indicator.getMacdSignalValue());
                assertNotEquals(null, indicator.getDatetime());
            });
        });
    }

//    @Test
    @DisplayName("Test get ema - another period")
    void a4TestGetSaved() {
        var indicators = dailyIndicatorService.getEma(AAPL, Interval.TWELVE_DATA_ONE_DAY, EmaPeriod.FIFTY, SeriesType.CLOSE);
        assertEquals(AAPL, indicators.getTicker());
        assertEquals(10, indicators.getEma().size());
        indicators.getEma().forEach(indicator -> {
            assertNotEquals(0.0, indicator.getEmaValue());
            assertThat(List.of(EmaPeriod.TWO_HUNDRED.getPeriod(), EmaPeriod.FIFTY.getPeriod()),
                    Matchers.contains(indicator.getEmaType()));
            assertNotEquals(null, indicator.getDatetime());
        });
    }


//    @Test
    @DisplayName("Test get saved indicator")
    void a5TestGetSaved() {
        var indicators = dailyIndicatorService.getEma(AAPL, Interval.TWELVE_DATA_ONE_DAY, EmaPeriod.FIFTY, SeriesType.CLOSE);

        assertEquals(AAPL, indicators.getTicker());
        assertEquals(Interval.TWELVE_DATA_ONE_DAY.getInterval(), indicators.getInterval());
        indicators.getEma().forEach(indicator -> {
            assertNotEquals(0.0, indicator.getEmaValue());
            assertThat(List.of(EmaPeriod.TWO_HUNDRED.getPeriod(), EmaPeriod.FIFTY.getPeriod()),
                    Matchers.contains(indicator.getEmaType()));
            assertNotEquals(null, indicator.getDatetime());
        });
        indicators.getMacd().forEach(indicator -> {
            assertNotEquals(0.0, indicator.getMacdHistValue());
            assertNotEquals(0.0, indicator.getMacdSignalValue());
            assertNotEquals(0.0, indicator.getMacdValue());
            assertNotEquals(null, indicator.getDatetime());
        });
        indicators.getRsi().forEach(indicator -> {
            assertNotEquals(0.0, indicator.getRsiValue());
            assertNotEquals(null, indicator.getDatetime());
        });
    }

    @TestConfiguration
    @EnableFeignClients(clients = TwelvedataApiClient.class)
    @Import({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
    @PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
    static class Config {

        @Bean
        DailyIndicatorService dailyIndicatorService(TwelvedataApiClient twelvedataApiClient,
                                                DailyIndicatorDataRepository dailyIndicatorDataRepository,
                                                DbUpdateService dbUpdateService) {
            return new DailyIndicatorService(twelvedataApiClient, dailyIndicatorDataRepository, dbUpdateService);
        }

        @Bean
        DbUpdateService dbUpdateService(DailyIndicatorDataRepository dailyIndicatorDataRepository,
                                        DailyEmaRepository dailyEmaRepository,
                                        DailyRsiRepository dailyRsiRepository,
                                        CandleRepository candleRepository,
                                        ChatRepository chatRepository) {
            return new DbUpdateServiceImpl(dailyIndicatorDataRepository, dailyEmaRepository, dailyRsiRepository,
                    candleRepository, chatRepository);
        }

    }
}