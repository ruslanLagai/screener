package com.home.project.stocks.service;

import com.home.project.stocks.model.candles.Candle;

import java.util.List;

/**
 * @author rlagay
 */
public interface HourlyScanService {
    void processStock(String ticker, String figi, List<Candle> candles);
}
