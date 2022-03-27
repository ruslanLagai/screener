package com.home.project.stocks.client;

import com.home.project.stocks.config.TwelveDataFeignConfig;
import com.home.project.stocks.model.api.CommonIndicator;
import com.home.project.stocks.model.candles.TwelveDataCandles;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "twelveDataClient", url = "${twelvedata.api.url}", configuration = TwelveDataFeignConfig.class)
public interface TwelvedataApiClient {

    @GetMapping("/ema?outputsize=5")
    CommonIndicator getEma(@RequestParam(value = "symbol") String ticker, @RequestParam String interval,
                           @RequestParam(value = "time_period") String emaPeriod);

    @GetMapping("/rsi?outputsize=5")
    CommonIndicator getRsi(@RequestParam(value = "symbol") String ticker, @RequestParam String interval,
                           @RequestParam(value = "time_period") String timePeriod);

    @GetMapping("/macd?outputsize=5")
    CommonIndicator getMacd(@RequestParam(value = "symbol") String ticker, @RequestParam String interval);

    @GetMapping("/time_series?outputsize=5")
    TwelveDataCandles getCandles(@RequestParam(value = "symbol") String ticker, @RequestParam String interval);

    @GetMapping("/time_series")
    TwelveDataCandles getCandles(@RequestParam(value = "symbol") String ticker, @RequestParam String interval,
                                 @RequestParam int outputsize);
}

