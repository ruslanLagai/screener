package com.home.project.stocks.service;

import com.home.project.stocks.config.FlywayConfig;
import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.repository.AbstractRepositoryTest;
import com.home.project.stocks.service.impl.MacdDivergenceStatisticService;
import com.home.project.stocks.telegram.TelegramBot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.telegram.telegrambots.starter.TelegramBotStarterConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * test for {@link MacdDivergenceStatisticService}
 *
 * @author rlagay
 */
@SpringBootTest
class MacdDivergenceStatisticServiceTest extends AbstractRepositoryTest {

    @MockBean
    private FlywayConfig flywayConfig;

    @MockBean
    private TelegramBot telegramBot;

    @MockBean
    private TelegramBotStarterConfiguration telegramBotStarterConfiguration;

    @Autowired
    private MacdDivergenceStatisticService statisticService;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.driver-class-name", container::getDriverClassName);
    }

    @Test
    @DisplayName("test no macd divergence")
    void analyzeStock() {
        var procResult = new ProcessingResult();
        statisticService.analyzeStock(procResult, Interval.TWELVE_DATA_ONE_DAY);
        assertEquals(0.0, procResult.getMacdDivergenceStatistics());
    }

    @Test
    @DisplayName("test macd ascending divergence")
    void analyzeStockWithAscMacd() {
        var procResult = new ProcessingResult();
        procResult.setTicker("GS");
        procResult.setMacdDivergence(ProcessingResult.Trend.ASCENDING);
        statisticService.analyzeStock(procResult, Interval.TWELVE_DATA_ONE_DAY);

        assertEquals(1.0, procResult.getMacdDivergenceStatistics());
    }

    @Test
    @DisplayName("test macd descending divergence")
    void analyzeStockWithDescMacd() {
        var procResult = new ProcessingResult();
        procResult.setTicker("VLO");
        procResult.setMacdDivergence(ProcessingResult.Trend.DESCENDING);
        statisticService.analyzeStock(procResult, Interval.TWELVE_DATA_ONE_DAY);

        assertEquals(1.0, procResult.getMacdDivergenceStatistics());
    }
}