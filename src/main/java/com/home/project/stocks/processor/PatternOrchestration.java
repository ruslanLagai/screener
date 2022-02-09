package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Log4j2
public class PatternOrchestration implements DailyProcessingOrchestrator {
    private List<PatternProcessor> patternProcessors;
    private Map<String, PatternProcessor> stocksProcessorMap;

    @Autowired
    public void setStocksProcessors(List<PatternProcessor> patternProcessors) {
        this.patternProcessors = patternProcessors;
        this.stocksProcessorMap = patternProcessors.stream()
                .collect(Collectors.toMap(PatternProcessor::getType, Function.identity()));
    }

    public void processStocks(@NonNull String ticker, @NonNull String figi,
                              List<Candle> candles, Candle lastCandle, ProcessingResult processingResult) {
        if (CollectionUtils.isEmpty(candles) || candles.size() < 4) {
            log.warn("Not enough candles to detect pattern, ticker {}, number of candles {}", ticker,
                    candles != null ? candles.size() : null);
            return;
        }
        var candlesToProcess = candles.stream().sorted(Comparator.comparing(Candle::getTime))
                        .collect(Collectors.toList());
        processingResult.setFigi(figi);
        processingResult.setTicker(ticker);
        patternProcessors.forEach(stocksProcessor -> {
            var isPattern = isPattern(figi, ticker, candlesToProcess.subList(candles.size() - 4, candles.size()),
                    stocksProcessor, processingResult);
            processingResult.initField(isPattern, stocksProcessor);
        });
    }

    private boolean isPattern(String figi, String ticker, List<Candle> candles, PatternProcessor patternProcessor,
                              ProcessingResult processingResult) {
        var procResult = patternProcessor.processStock(figi, ticker, candles);
        var isPattern = !procResult.isEmpty();
        if (isPattern) {
            processingResult.getProcessedCandles().putAll(procResult);
        }
        return isPattern;
    }
}
