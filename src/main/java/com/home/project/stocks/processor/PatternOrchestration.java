package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Log4j2
public class PatternOrchestration implements Orchestration {
    private List<PatternProcessor> patternProcessors;
    private Map<String, PatternProcessor> stocksProcessorMap;

    @Autowired
    public void setStocksProcessors(List<PatternProcessor> patternProcessors) {
        this.patternProcessors = patternProcessors;
        this.stocksProcessorMap = patternProcessors.stream()
                .collect(Collectors.toMap(PatternProcessor::getType, Function.identity()));
    }

    public ProcessingResult processStocks(@NonNull String ticker, @NonNull String figi,
                                          Candle[] candles, Candle candle) {
        var processingResult = new ProcessingResult();
        processingResult.setFigi(figi);
        processingResult.setTicker(ticker);
        patternProcessors.forEach(stocksProcessor -> {
                    var isPattern = isPattern(figi, ticker, candles, stocksProcessor, processingResult);
                    processingResult.initField(isPattern, stocksProcessor);
        });
        return processingResult;
    }

    private boolean isPattern(String figi, String ticker, Candle[] candles, PatternProcessor patternProcessor,
                              ProcessingResult processingResult) {
        var procResult = patternProcessor.processStock(figi, ticker, candles);
        var isPattern = !procResult.isEmpty();
        if (isPattern) {
            processingResult.getProcessedCandles().addAll(procResult);
        }
        return isPattern;
    }
}
