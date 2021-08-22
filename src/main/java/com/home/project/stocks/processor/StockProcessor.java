package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.repository.RepositorySaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Class to implement processing logic: patters & indicators
 */
@Component
public class StockProcessor {

    private final List<Orchestration> orchestrations;
    private final RepositorySaver repositorySaver;

    @Autowired
    public StockProcessor(List<Orchestration> orchestrations,
                          RepositorySaver repositorySaver) {
        this.orchestrations = orchestrations;
        this.repositorySaver = repositorySaver;
    }

    public void processStock(String ticker, String figi, Candle[] candles) {
        var processingResult = new ProcessingResult();
        var lastCandle = candles[candles.length - 1];
        orchestrations.forEach(orchestration ->
                orchestration.processStocks(ticker, figi, candles, lastCandle, processingResult));
        repositorySaver.populateIndexes(processingResult, lastCandle);
    }



}
