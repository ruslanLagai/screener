package com.home.project.stocks;

import com.home.project.stocks.config.FlywayConfig;
import com.home.project.stocks.model.entity.StocksToScan;
import com.home.project.stocks.repository.DailyProcessedIndicatorRepository;
import com.home.project.stocks.service.DailyScanService;
import com.home.project.stocks.telegram.TelegramBot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class StocksApplicationTests {

    @Container
    private static final MySQLContainer<?> container = new MySQLContainer<>("mysql:8");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.driver-class-name", container::getDriverClassName);
    }

    static {
        container.start();
    }

    @MockBean
    private FlywayConfig flywayConfig;

    @Autowired
    private List<DailyScanService> dailyScanService;

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private DailyProcessedIndicatorRepository indicatorRepository;

    @Test
    void contextLoads() {
        var stocks = List.of(
                StocksToScan.builder().name("").ticker("PPL").build()
        );

        stocks.forEach(stock ->
                dailyScanService.forEach(service ->
                        service.processStock(stock.getTicker(), stock.getFigi())));

        var indicator = indicatorRepository.getByTicker("PPL");
        telegramBot.sendNotification();
    }

}
