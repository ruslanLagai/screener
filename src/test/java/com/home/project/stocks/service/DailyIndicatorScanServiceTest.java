package com.home.project.stocks.service;

import com.home.project.stocks.config.FlywayConfig;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.entity.DailyEma;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.repository.DailyProcessedIndicatorRepository;
import com.home.project.stocks.service.impl.DailyIndicatorScanService;
import com.home.project.stocks.telegram.TelegramBot;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.starter.TelegramBotStarterConfiguration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = DailyIndicatorScanServiceTest.Config.class)
class DailyIndicatorScanServiceTest {

    private static final String DRI = "DRI";
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
    private IndicatorService indicatorService;

    @MockBean
    private CandlesService candlesService;

    static {
        container.start();
    }

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
    @DisplayName("test ema indicator")
    void processStock() throws InterruptedException {
        when(indicatorService.getEma(eq(DRI), any(), any(), any())).thenReturn(mockEma(DRI));
        when(candlesService.getCandles(eq(DRI), any())).thenReturn(mockCandles());
        when(candlesService.getHistoricalCandles(eq(DRI), any(), anyInt())).thenReturn(mockCandles(120.0));

        dailyIndicatorScanService.processStock(DRI, "");
        assertTrue(countDownLatch.await(60, TimeUnit.SECONDS));

        var savedIndicator = indicatorRepository.getByTicker(DRI);

        assertAll(() -> {
            assertNotNull(savedIndicator);
            assertNotNull(savedIndicator.getDate());
            assertEquals(DRI, savedIndicator.getTicker());
            assertEquals(1, savedIndicator.getEmaData().size());
            assertEquals(ProcessingResult.LevelType.SUPPORT, savedIndicator.getEmaData().get(0).getLevelType());
            assertEquals(110, savedIndicator.getEmaData().get(0).getEmaValue());
            assertEquals("200", savedIndicator.getEmaData().get(0).getEmaType());
        });
    }

    @Test
    @DisplayName("No candles - invalid ticker")
    void testInvalidTicker() {
        when(indicatorService.getEma(eq(DRI + "ewr"), any(), any(), any())).thenReturn(mockEma(DRI + "ewr"));
        when(candlesService.getCandles(eq(DRI + "ewr"), any())).thenReturn(List.of());

        dailyIndicatorScanService.processStock(DRI + "ewr", "");

        var savedIndicator = indicatorRepository.getByTicker(DRI + "ewr");
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

    private List<Candle> mockCandles() {
        return List.of(
                Candle.builder()
                        .c(112)
                        .l(111)
                        .datetime(LocalDateTime.now())
                        .interval("1day")
                        .build(),
                Candle.builder()
                        .c(113)
                        .l(112)
                        .datetime(LocalDateTime.now().minusDays(1))
                        .interval("1day")
                        .build()
        );
    }

    private List<Candle> mockCandles(double value) {
        List<Candle> candles = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            candles.add(Candle.builder()
                    .c(value)
                    .o(value)
                    .l(value)
                    .h(value)
                    .interval("1day")
                    .build());
        }
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