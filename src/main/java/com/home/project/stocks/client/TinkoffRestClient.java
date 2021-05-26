package com.home.project.stocks.client;

import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;

import com.home.project.stocks.model.candles.*;

public interface TinkoffRestClient {

    public ResponseEntity<StockByTicker> getStocks();

    public ResponseEntity<StockByTicker> getStockByTicker(String ticker);

    public ResponseEntity<CandlesByFigi> getCandles(String figi, DateTime from, DateTime to, Interval interval);

//    public ResponseEntity
}
