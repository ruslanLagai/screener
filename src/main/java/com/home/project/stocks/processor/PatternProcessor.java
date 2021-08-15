package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import org.springframework.util.MultiValueMap;

public interface PatternProcessor {
    MultiValueMap<Processors, Candle> processStock(String figi, String ticker, Candle[] candles);
    default String getType() {
        return this.getClass().getName();
    }

    enum Processors {
        DODGE,
        HAMMER
    }
}
