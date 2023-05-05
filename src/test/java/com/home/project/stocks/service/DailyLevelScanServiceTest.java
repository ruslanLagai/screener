package com.home.project.stocks.service;

import com.home.project.stocks.client.TwelvedataApiClient;
import com.home.project.stocks.config.FlywayConfig;
import com.home.project.stocks.mapper.MacdDataMapper;
import com.home.project.stocks.model.entity.Levels;
import com.home.project.stocks.model.entity.WeeklyLevel;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.repository.AbstractRepositoryTest;
import com.home.project.stocks.repository.ProcessedLevelsRepository;
import com.home.project.stocks.repository.WeeklyLevelsRepository;
import com.home.project.stocks.telegram.TelegramBot;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.home.project.stocks.utils.TestUtils.readCandles;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


/**
 * @author rlagay
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = DailyLevelScanServiceTest.Config.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
class DailyLevelScanServiceTest extends AbstractRepositoryTest {

    private static final CountDownLatch countDownLatch = new CountDownLatch(1);
    private static final String AMZN_1 = "AMZN_1";
    private static final String AMZN_2 = "AMZN_2";
    private static final String AMZN_3 = "AMZN_3";
    private static final String AMZN_4 = "AMZN_4";

    @MockBean
    private FlywayConfig flywayConfig;

    @MockBean
    private TelegramBot telegramBot;

    @MockBean
    private TelegramBotStarterConfiguration telegramBotStarterConfiguration;

    @MockBean
    private WeeklyLevelsRepository weeklyLevelsRepository;

    @MockBean
    private TwelvedataApiClient twelvedataApiClient;

    @MockBean
    private MacdDataMapper macdDataMapper;

    @Autowired
    private DailyScanService dailyLevelScanService;

    @Autowired
    private ProcessedLevelsRepository processedLevelsRepository;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.driver-class-name", container::getDriverClassName);
    }

    @Test
    @DisplayName("Test no level")
    void a1processStockNoLevel() throws InterruptedException {
        when(twelvedataApiClient.getCandles(eq(AMZN_1), any(), anyInt())).thenReturn(readCandles("templates/levels/amzn-resistance-candles.json"));
        when(weeklyLevelsRepository.findByTicker(eq(AMZN_1))).thenReturn(mockNoLevels());

        dailyLevelScanService.processStock(AMZN_1, "");

        countDownLatch.await(5, TimeUnit.SECONDS);
        var saved = processedLevelsRepository.findByTicker(AMZN_1);
        assertNull(saved);
    }

    @Test
    @DisplayName("Test resistance level")
    void a2processStock() throws InterruptedException {
        when(twelvedataApiClient.getCandles(eq(AMZN_2), any(), anyInt())).thenReturn(readCandles("templates/levels/amzn-resistance-candles.json"));
        when(weeklyLevelsRepository.findByTicker(eq(AMZN_2))).thenReturn(mockResistanceLevels());

        dailyLevelScanService.processStock(AMZN_2, "");

        assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
        var saved = processedLevelsRepository.findByTicker(AMZN_2);
        assertEquals(AMZN_2, saved.getTicker());
        assertEquals(3530, saved.getLevel());
        assertEquals(ProcessingResult.LevelType.RESISTANCE, saved.getLevelType());
        assertEquals(3500.3, saved.getClosePrice());
    }

    @Test
    @DisplayName("Test support level")
    void a3processStockSupport() throws InterruptedException {
        when(twelvedataApiClient.getCandles(eq(AMZN_3), any(), anyInt())).thenReturn(readCandles("templates/levels/amzn-support-candles.json"));
        when(weeklyLevelsRepository.findByTicker(eq(AMZN_3))).thenReturn(mockSupportLevels());

        dailyLevelScanService.processStock(AMZN_3, "");

        assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
        var saved = processedLevelsRepository.findByTicker(AMZN_3);
        assertEquals(AMZN_3, saved.getTicker());
        assertEquals(2680, saved.getLevel());
        assertEquals(ProcessingResult.LevelType.SUPPORT, saved.getLevelType());
        assertEquals(2720.29, saved.getClosePrice());
    }

    @Test
    @DisplayName("Test null level")
    void a4processNullLevel() throws InterruptedException {
        when(twelvedataApiClient.getCandles(eq(AMZN_4), any(), anyInt())).thenReturn(readCandles("templates/levels/amzn-support-candles.json"));
        when(weeklyLevelsRepository.findByTicker(eq(AMZN_4))).thenReturn(null);

        dailyLevelScanService.processStock(AMZN_4, "");

        countDownLatch.await(5, TimeUnit.SECONDS);
        var saved = processedLevelsRepository.findByTicker(AMZN_4);
        assertNull(saved);
    }

    private WeeklyLevel mockNoLevels() {
        return WeeklyLevel.builder()
                .ticker(AMZN_1)
                .levels(Set.of(
                        Levels.builder()
                                .value(3500)
                                .build(),
                        Levels.builder()
                                .value(3180)
                                .build(),
                        Levels.builder()
                                .value(2650)
                                .build()
                ))
                .build();
    }

    private WeeklyLevel mockResistanceLevels() {
        return WeeklyLevel.builder()
                .ticker(AMZN_2)
                .levels(Set.of(
                        Levels.builder()
                                .value(3440)
                                .build(),
                        Levels.builder()
                                .value(3540)
                                .build(),
                        Levels.builder()
                                .value(3530)
                                .build()
                ))
                .build();
    }

    private WeeklyLevel mockSupportLevels() {
        return WeeklyLevel.builder()
                .ticker(AMZN_3)
                .levels(Set.of(
                        Levels.builder()
                                .value(2680)
                                .build(),
                        Levels.builder()
                                .value(2670)
                                .build(),
                        Levels.builder()
                                .value(2800)
                                .build()
                ))
                .build();
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

        @After("execution(* com.home.project.stocks.repository.ProcessedLevelsRepository.save(*))")
        public void after() {
            countDownLatch.countDown();
        }
    }
}