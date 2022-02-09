package com.home.project.stocks.client;

import com.home.project.stocks.config.TinkoffFeignConfig;
import com.home.project.stocks.model.candles.StockByTicker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author rlagay
 */
@FeignClient(name = "tinkoffClient", url = "${alpha.vantage.url}", configuration = TinkoffFeignConfig.class)
public interface TinkoffClient {

    @GetMapping("/market/search/by-ticker")
    StockByTicker getStockByTicker(@RequestParam String ticker);

    @GetMapping("/market/candles")
    StockByTicker getCandlesByFigi(@RequestParam String figi, @RequestParam String from, @RequestParam String to,
                                   @RequestParam String interval);

}
