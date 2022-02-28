package com.home.project.stocks.service;

import com.home.project.stocks.model.aplha.vantage.Interval;
import com.home.project.stocks.model.candles.Candle;

import java.util.List;

/**
 * Interface to interact with Alpha Vantage to get indicator data
 */
public interface CandlesService {

    List<Candle> getCandles(String ticker, Interval interval);

}
