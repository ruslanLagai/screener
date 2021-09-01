package com.home.project.stocks.processor;

import com.home.project.stocks.model.aplha.vantage.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.service.RepositoryService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Class to implement processing logic: patters & indicators
 */
@Component
@Log4j2
public class StockProcessor {

    private final List<Orchestration> orchestrations;
    private final RepositoryService repositoryService;

    @Autowired
    public StockProcessor(List<Orchestration> orchestrations,
                          RepositoryService repositoryService) {
        this.orchestrations = orchestrations;
        this.repositoryService = repositoryService;
    }

    public void processStock(String ticker, String figi, Map<Date, Candle> candles) {
        if (candles == null || candles.size() < 5) {
            log.warn(String.format("Not enough candles for stock, ticker %s, figi %s", ticker, figi));
            return;
        }
        var processingResult = new ProcessingResult();
        var lastDate = candles.keySet().stream().max(Comparator.naturalOrder());
        if (lastDate.isPresent()) {
            orchestrations.forEach(orchestration ->
                    orchestration.processStocks(ticker, figi, candles, lastDate.get(), processingResult));
            repositoryService.populateIndexes(processingResult, candles.get(lastDate.get()));
        } else {
            log.warn(String.format("Failed to retrieve last date for candles, ticker %s", ticker));
        }
    }



}
