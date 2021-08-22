package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import lombok.NonNull;

/**
 * Basic interface for processing flows
 */
public interface Orchestration {

    void processStocks(@NonNull String ticker, @NonNull String figi,
                                   Candle[] candles, Candle candle, ProcessingResult processingResult);
}
