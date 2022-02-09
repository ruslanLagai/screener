package com.home.project.stocks.service;

import com.home.project.stocks.model.candles.Candle;

import java.util.List;

/**
 * @author rlagay
 */
public interface DailyScanService {
    void processStock(String ticker, String figi, List<Candle> candles);

}
