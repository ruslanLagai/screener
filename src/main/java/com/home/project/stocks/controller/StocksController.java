package com.home.project.stocks.controller;

import com.home.project.stocks.client.TinkoffRestClient;
import com.home.project.stocks.model.candles.StockByTicker;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
@Log4j2
public class StocksController {

    private final TinkoffRestClient client;

    public StocksController(TinkoffRestClient client) {
        this.client = client;
    }

    @GetMapping
    public ResponseEntity<StockByTicker> getStocks() {
        return client.getStocks();
    }


}
