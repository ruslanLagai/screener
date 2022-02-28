package com.home.project.stocks.service;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import lombok.NonNull;

import java.util.List;

/**
 * Basic interface for processing flows on hourly basis
 */
public interface HourlyProcessingOrchestrator {

    void processStocks(@NonNull String ticker, @NonNull String figi,
                       List<Candle> candles, Candle lastCandle, ProcessingResult processingResult);
}
