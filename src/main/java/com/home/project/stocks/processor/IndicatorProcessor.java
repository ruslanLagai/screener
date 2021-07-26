package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.indicators.IndicatorProcessingResult;
import com.home.project.stocks.model.indicators.ParsedIndicator;

public interface IndicatorProcessor {
    void processIndicator(ParsedIndicator indicator, Candle candle, IndicatorProcessingResult processingResult);
}
