package com.home.project.stocks.scheduled;

import com.home.project.stocks.repository.StocksToScanRepository;
import com.home.project.stocks.service.DailyScanService;
import com.home.project.stocks.telegram.TelegramBot;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Class that starts processing on schedule
 */
@Component
@Slf4j
public class CandlesRequester implements ScheduledRequester {

    private final List<DailyScanService> dailyScanService;
    private final StocksToScanRepository stocksToScanRepository;
    private final TelegramBot telegramBot;

    @Autowired
    public CandlesRequester(List<DailyScanService> dailyScanService,
                            StocksToScanRepository stocksToScanRepository,
                            TelegramBot telegramBot) {
        this.dailyScanService = dailyScanService;
        this.stocksToScanRepository = stocksToScanRepository;
        this.telegramBot = telegramBot;
    }

    @Override
    @Scheduled(cron = "${screener.pattern.cron}")
    @Profile("!test")
    @SneakyThrows
    public void requestData() {
        var stocks = stocksToScanRepository.findAll();

        stocks.forEach(stock -> {
            log.info("Start processing stock, ticker {}", stock.getTicker());
            dailyScanService.forEach(service -> service.processStock(stock.getTicker(), stock.getFigi()));
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        });
        telegramBot.sendNotification();
    }
}
