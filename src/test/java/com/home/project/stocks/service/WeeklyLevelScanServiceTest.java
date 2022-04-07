package com.home.project.stocks.service;

import com.home.project.stocks.client.TwelvedataApiClient;
import com.home.project.stocks.processor.WeeklyLevelProcessor;
import com.home.project.stocks.repository.AbstractRepositoryTest;
import com.home.project.stocks.repository.WeeklyLevelsRepository;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author rlagay
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WeeklyLevelScanServiceTest.Config.class, AbstractRepositoryTest.Config.class},
        initializers = AbstractRepositoryTest.Config.class)
class WeeklyLevelScanServiceTest extends AbstractRepositoryTest {

    private static final CountDownLatch countDownLatch = new CountDownLatch(1);

    static {
        container.start();
    }

    @Autowired
    private WeeklyScanService weeklyLevelScanService;

    @Autowired
    private WeeklyLevelsRepository weeklyLevelsRepository;

    @Test
    @DisplayName("Basic test")
    void processStock() throws InterruptedException {
        weeklyLevelScanService.processStock("BKNG");

        assertTrue(countDownLatch.await(10, TimeUnit.SECONDS));
        var saved = weeklyLevelsRepository.findByTicker("BKNG");
        assertNotNull(saved);
        assertEquals("BKNG", saved.getTicker());
        assertFalse(saved.getLevels().isEmpty());
    }

    @Test
    @DisplayName("Invalid ticker test")
    void processStockInvalidTicker() throws InterruptedException {
        weeklyLevelScanService.processStock("BKNGG");

        assertTrue(countDownLatch.await(3, TimeUnit.SECONDS));
        var saved = weeklyLevelsRepository.findByTicker("BKNGG");
        assertNull(saved);
    }

    @TestConfiguration
    @ComponentScan(basePackages = {"com.home.project.stocks.service"})
    @EnableFeignClients(clients = TwelvedataApiClient.class)
    @EnableAspectJAutoProxy
    @Import({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
    static class Config {

        @Bean
        WeeklyLevelProcessor weeklyLevelProcessor() {
            return new WeeklyLevelProcessor();
        }

        @Bean
        public HelperAspect helperAspect() {
            return new HelperAspect();
        }
    }

    @Aspect
    protected static class HelperAspect {

        @After("execution(* com.home.project.stocks.service.DbUpdateService.saveWeeklyLevels(*,*))")
        public void after() {
            countDownLatch.countDown();
        }
    }
}