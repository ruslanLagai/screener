package com.home.project.stocks.processor;


import com.home.project.stocks.model.candles.Candle;

import java.util.List;
import java.util.Map;

public interface PatternProcessor {
    Map<Processors, Candle> processStock(String figi, String ticker, List<Candle> candles);

    default String getType() {
        return this.getClass().getName();
    }

    enum Processors {
        DODGE,
        HAMMER
    }
}
