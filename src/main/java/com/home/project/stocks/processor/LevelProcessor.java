package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;

import java.util.List;
import java.util.Set;

/**
 * @author rlagay
 */
public interface LevelProcessor {

    Set<Double> processStock(String ticker, List<Candle> candles);
}
