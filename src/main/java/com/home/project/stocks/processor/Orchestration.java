package com.home.project.stocks.processor;

import com.home.project.stocks.model.aplha.vantage.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import lombok.NonNull;

import java.util.Date;
import java.util.Map;

/**
 * Basic interface for processing flows
 */
public interface Orchestration {

    void processStocks(@NonNull String ticker, @NonNull String figi,
                       Map<Date, Candle> candles, Date lastDate, ProcessingResult processingResult);
}
