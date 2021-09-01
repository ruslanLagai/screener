package com.home.project.stocks.processor;

import com.home.project.stocks.model.aplha.vantage.Candle;
import org.springframework.util.MultiValueMap;

import java.util.Date;
import java.util.Map;

public interface PatternProcessor {
    MultiValueMap<Processors, Candle> processStock(String figi, String ticker, Map<Date, Candle> candles);
    default String getType() {
        return this.getClass().getName();
    }

    enum Processors {
        DODGE,
        HAMMER
    }
}
