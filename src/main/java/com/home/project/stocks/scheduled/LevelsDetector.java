package com.home.project.stocks.scheduled;

import com.home.project.stocks.repository.StocksToScanRepository;
import com.home.project.stocks.service.WeeklyScanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author rlagay
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class LevelsDetector implements Scheduler{

    private final List<WeeklyScanService> weeklyScanService;
    private final StocksToScanRepository stocksToScanRepository;

    @Override
    @Scheduled(cron = "${screener.level.cron}")
    public void requestData() {
        var stocks = stocksToScanRepository.findAll();
        stocks.forEach(stocksToScan ->
                weeklyScanService.forEach(service -> service.processStock(stocksToScan.getTicker())));
    }
}
