package com.home.project.stocks.processor;

import com.home.project.stocks.model.aplha.vantage.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.indicators.ParsedIndicator;

public interface IndicatorProcessor {
    void processIndicator(ParsedIndicator indicator, Candle candle, ProcessingResult processingResult);
}
