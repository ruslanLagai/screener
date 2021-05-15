package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PatternOrchestration {
    private List<StocksProcessor> stocksProcessors;
    private Map<String, StocksProcessor> stocksProcessorMap;

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
        //todo check already sent dodges

        //todo call to microservice with result
        return result;
    }

    private boolean isPattern(Map.Entry<String, Candle[]> i, StocksProcessor stocksProcessor,
                              ProcessingResult processingItem) {
        var procResult = stocksProcessor.processStock(i.getKey(), "", i.getValue());
        var isPattern = !procResult.isEmpty();
        if (isPattern) {
            processingItem.getProcessedCandles().putAll(procResult);
        }
        return isPattern;
    }
}
