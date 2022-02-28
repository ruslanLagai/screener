package com.home.project.stocks.service.impl;

import com.home.project.stocks.client.TwelvedataApiClient;
import com.home.project.stocks.model.aplha.vantage.Interval;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.candles.TwelveDataCandles;
import com.home.project.stocks.service.CandlesService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author rlagay
 */
@Slf4j
@Service
public class DailyCandlesService implements CandlesService {

    private final TwelvedataApiClient twelvedataApiClient;

    public DailyCandlesService(TwelvedataApiClient twelvedataApiClient) {
        this.twelvedataApiClient = twelvedataApiClient;
    }

    @Override
    public List<Candle> getCandles(String ticker, Interval interval) {
        log.debug("Retrieving candles for ticker {}", ticker);
        List<Candle> candles;
        try {
            candles = Optional.ofNullable(twelvedataApiClient.getCandles(ticker, interval.getInterval()))
                    .map(TwelveDataCandles::getValues)
                    .orElse(Collections.emptyList());
        } catch (FeignException e) {
            log.error("Failed to retrieve candles for ticker {}, status {}", ticker, e.status());
            candles = Collections.emptyList();
        }

        return candles;
    }
}
