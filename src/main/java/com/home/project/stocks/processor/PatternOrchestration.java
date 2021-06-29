package com.home.project.stocks.processor;

import com.home.project.stocks.client.NotifierClient;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.service.RepositoryService;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Log4j2
public class PatternOrchestration {
    private List<StocksProcessor> stocksProcessors;
    private Map<String, StocksProcessor> stocksProcessorMap;
    private RepositoryService repositoryService;
    private NotifierClient notifierClient;

    @Autowired
    public void setNotifierClient(NotifierClient notifierClient) {
        this.notifierClient = notifierClient;
    }

    @Autowired
    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Autowired
    public void setStocksProcessors(List<StocksProcessor> stocksProcessors) {
        this.stocksProcessors = stocksProcessors;
        this.stocksProcessorMap = stocksProcessors.stream()
                .collect(Collectors.toMap(StocksProcessor::getType, Function.identity()));
    }

    public Collection<ProcessingResult> processStocks(@NonNull Map<String, Candle[]> data) {
        Set<ProcessingResult> result = null;
        for (var i : data.entrySet()) {
            var processingItem = new ProcessingResult();
            processingItem.setFigi(i.getKey());
            var processingItems = stocksProcessors.stream()
                    .map(stocksProcessor -> {
                        var isPattern = isPattern(i, stocksProcessor, processingItem);
                        processingItem.initField(isPattern, stocksProcessor);
                        return processingItem; })
                    .collect(Collectors.toSet());
            result = processingItems.stream()
                    .filter(ProcessingResult::shouldBeSent)
                    .collect(Collectors.toSet());
        }
        if (result != null && !result.isEmpty()) {
            result = repositoryService.save(result);
            var response = notifierClient.notifyUser(result);
            if (response.getStatusCode() != HttpStatus.OK) {
               log.error("Failed to process candles, status code: " + response.getStatusCode());
            }
        }
        return result;
    }

    private boolean isPattern(Map.Entry<String, Candle[]> i, StocksProcessor stocksProcessor,
                              ProcessingResult processingItem) {
        var procResult = stocksProcessor.processStock(i.getKey(), "", i.getValue());
        var isPattern = !procResult.isEmpty();
        if (isPattern) {
            processingItem.getProcessedCandles().addAll(procResult);
        }
        return isPattern;
    }
}
