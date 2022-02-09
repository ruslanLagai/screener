package com.home.project.stocks.service;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.processor.HourlyProcessingOrchestrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;

/**
 * Class to implement processing logic: indicators on hour time frame
 *
 *  @author rlagay
 */
@Service
@Slf4j
public class IndicatorScanService implements HourlyScanService {

    private final List<HourlyProcessingOrchestrator> indicatorOrchestration;
    private final RepositoryService repositoryService;

    public IndicatorScanService(List<HourlyProcessingOrchestrator> indicatorOrchestration,
                                RepositoryService repositoryService) {
        this.indicatorOrchestration = indicatorOrchestration;
        this.repositoryService = repositoryService;
    }


    /**
     *
     * @param ticker ticker
     * @param figi figi
     * @param candles sorted candles by date
     */
    @Override
    public void processStock(String ticker, String figi, List<Candle> candles) {
        if (CollectionUtils.isEmpty(candles)) {
            log.warn("Empty candles for {}, figi {}", ticker, figi);
            return;
        }
        var processingResult = new ProcessingResult();
        processingResult.setFigi(figi);
        processingResult.setTicker(ticker);

        candles.stream().max(Comparator.comparing(Candle::getTime))
                .ifPresentOrElse(lastCandle -> {
                    indicatorOrchestration.forEach(processingOrchestrator ->
                            processingOrchestrator.processStocks(ticker, figi, candles, lastCandle, processingResult));
                    repositoryService.populateIndicatorIndexes(processingResult, lastCandle);
                }, () -> log.warn("Failed to retrieve last date for candles, ticker {}", ticker));
    }
}
