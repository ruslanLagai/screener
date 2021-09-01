package com.home.project.stocks.client;

import com.home.project.stocks.config.FeingConfig;
import com.home.project.stocks.model.aplha.vantage.Candles;
import com.home.project.stocks.model.aplha.vantage.CommonIndicator;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "alphaVantageClient", url = "${alpha.vantage.url}", configuration = FeingConfig.class)
public interface AlphaVantageApiClient {

    @GetMapping
    CommonIndicator getEma(@RequestParam String function, @RequestParam(value = "symbol") String ticker,
                           @RequestParam String interval, @RequestParam(value = "time_period") String timePeriod,
                           @RequestParam(value = "series_type") String seriesType, @RequestParam String apikey);

    @GetMapping
    CommonIndicator getRsi(@RequestParam String function, @RequestParam(value = "symbol") String ticker,
               @RequestParam String interval, @RequestParam(value = "time_period") String timePeriod,
               @RequestParam(value = "series_type") String seriesType, @RequestParam String apikey);

    @GetMapping
    CommonIndicator getMacd(@RequestParam String function, @RequestParam(value = "symbol") String ticker,
                           @RequestParam String interval, @RequestParam(value = "series_type") String seriesType,
                           @RequestParam String apikey);

    @GetMapping
    Candles getCandles(@RequestParam String function, @RequestParam(value = "symbol") String ticker,
                       @RequestParam String apikey);

}

