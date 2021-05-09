package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;

import java.util.Map;

public interface StocksProcessor {
    Map<Integer, Candle> processStock(String figi, String ticker, Candle[] candles);
    default String getType() {
        return this.getClass().getName();
    }
}
