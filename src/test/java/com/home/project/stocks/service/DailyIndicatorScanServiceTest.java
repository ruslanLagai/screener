package com.home.project.stocks.service;

import com.home.project.stocks.client.TwelvedataApiClient;
import com.home.project.stocks.config.FlywayConfig;
import com.home.project.stocks.model.api.CommonIndicator;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.candles.TwelveDataCandles;
import com.home.project.stocks.model.entity.DailyEma;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.repository.DailyProcessedIndicatorRepository;
import com.home.project.stocks.service.impl.DailyIndicatorScanService;
import com.home.project.stocks.telegram.TelegramBot;
import com.home.project.stocks.utils.TestUtils;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.telegram.telegrambots.starter.TelegramBotStarterConfiguration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Class to test {@link DailyIndicatorScanService} with test containers
 *
 * @author rlagay
 */
@SpringBootTest
@Testcontainers
@SpringJUnitConfig(classes = DailyIndicatorScanServiceTest.Config.class)
class DailyIndicatorScanServiceTest {

    private static final String PNW = "PNW";
    private static final CountDownLatch countDownLatch = new CountDownLatch(1);

    @Container
    protected static MySQLContainer<?> container = new MySQLContainer<>("mysql:8");

    @MockBean
    private FlywayConfig flywayConfig;

    @MockBean
    private TelegramBot telegramBot;

    @MockBean
    private TelegramBotStarterConfiguration telegramBotStarterConfiguration;

    @MockBean
    private TwelvedataApiClient apiClient;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.driver-class-name", container::getDriverClassName);
    }

    @BeforeEach
    public void setUp() {
    }

    @Autowired
    private DailyScanService dailyIndicatorScanService;

    @Autowired
    private DailyProcessedIndicatorRepository indicatorRepository;

    @Test
    @DisplayName("test indicator processing")
    void processStock() throws InterruptedException {
        var macd = TestUtils.readData("templates/indicators/macd.json", CommonIndicator.class);
        var candles = TestUtils.readData("templates/indicators/candles.json", TwelveDataCandles.class);
        var ema = TestUtils.readData("templates/indicators/weekly-ema.json", CommonIndicator.class);
        when(apiClient.getMacd(eq(PNW), any())).thenReturn(macd);
        when(apiClient.getCandles(eq(PNW), any(), anyInt())).thenReturn(candles);
        when(apiClient.getEma(eq(PNW), any(), any())).thenReturn(ema);

        dailyIndicatorScanService.processStock(PNW, "");
        assertTrue(countDownLatch.await(60, TimeUnit.SECONDS));

        var savedIndicator = indicatorRepository.getByTicker(PNW);

        assertAll(() -> {
            assertNotNull(savedIndicator);
            assertNotNull(savedIndicator.getDate());
            assertEquals(PNW, savedIndicator.getTicker());
            assertEquals(1, savedIndicator.getEmaData().size());
            assertEquals(ProcessingResult.LevelType.RESISTANCE, savedIndicator.getEmaData().get(0).getLevelType());
            assertEquals(77.81, savedIndicator.getEmaData().get(0).getEmaValue());
            assertEquals("200", savedIndicator.getEmaData().get(0).getEmaType());
            assertEquals(ProcessingResult.Trend.DESCENDING.name(), savedIndicator.getMacdBarTrend());
            assertEquals(ProcessingResult.Trend.NO_SIGN.name(), savedIndicator.getMacdSignalTrend());
            assertEquals(ProcessingResult.Trend.DESCENDING.name(), savedIndicator.getMacdDiverTrend());
            assertEquals(77.32, savedIndicator.getClosePrice());
        });
    }

    @Test
    @DisplayName("No candles - invalid ticker")
    void testInvalidTicker() {
        var macd = TestUtils.readData("templates/indicators/macd.json", CommonIndicator.class);
        var ema = TestUtils.readData("templates/indicators/weekly-ema.json", CommonIndicator.class);
        when(apiClient.getMacd(eq("DRI"), any())).thenReturn(macd);
        when(apiClient.getCandles(eq("DRI"), any(), anyInt())).thenReturn(mockCandles());
        when(apiClient.getEma(eq("DRI"), any(), any())).thenReturn(ema);

        dailyIndicatorScanService.processStock("DRI" + "ewr", "");

        var savedIndicator = indicatorRepository.getByTicker("DRI");
        assertNull(savedIndicator);
    }

    private ParsedIndicator mockEma(String ticker) {
        return ParsedIndicator.builder()
                .ticker(ticker)
                .interval("1day")
                .ema(List.of(
                        DailyEma.builder()
                                .emaValue(110)
                                .emaType("200")
                                .datetime(LocalDateTime.now())
                                .build(),
                        DailyEma.builder()
                                .emaValue(105)
                                .emaType("200")
                                .datetime(LocalDateTime.now().minusDays(1))
                                .build()
                ))
                .build();
    }

    private TwelveDataCandles mockCandles() {
        var candles = new TwelveDataCandles();
        candles.setValues(List.of(Candle.builder()
                .v(20)
                .o(20)
                .c(20)
                .build()));
        return candles;
    }

    @TestConfiguration
    @EnableAspectJAutoProxy
    static class Config {

        @Bean
        public HelperAspect helperAspect() {
            return new HelperAspect();
        }

    }

    @Aspect
    protected static class HelperAspect {

        @After("execution(* com.home.project.stocks.service.DbUpdateService.saveIndicatorData(*))")
        public void after() {
            countDownLatch.countDown();
        }
    }

}