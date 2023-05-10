package com.home.project.stocks.service.impl;

import com.home.project.stocks.client.TwelvedataApiClient;
import com.home.project.stocks.mapper.CandlesMapper;
import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.candles.TwelveDataCandles;
import com.home.project.stocks.repository.DailyCandleRepository;
import com.home.project.stocks.service.CandlesService;
import com.home.project.stocks.service.DbUpdateService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author rlagay
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyCandlesService implements CandlesService {

    private final TwelvedataApiClient twelvedataApiClient;
    private final DailyCandleRepository dailyCandleRepository;
    private final DbUpdateService dbUpdateService;
    private final CandlesMapper candlesMapper;

    @Override
    public List<Candle> getCandles(String ticker, Interval interval) {
        return getHistoricalCandles(ticker, interval, 5);
    }

    @Override
    public List<Candle> getHistoricalCandles(String ticker, Interval interval, int total) {
        log.debug("Retrieving candles for ticker {}", ticker);
        List<Candle> candles;

        var saved = Optional.ofNullable(dailyCandleRepository.findByTickerAndInterval(ticker, interval.getInterval()))
                .orElse(Collections.emptyList());
        if (saved.size() >= total) {
            log.debug("Retrieved candles from DB, ticker {}", ticker);
            return saved.stream().map(candlesMapper::toRestCandle).collect(Collectors.toList());
        }
        try {
            candles = Optional.ofNullable(twelvedataApiClient.getCandles(ticker, interval.getInterval(), total))
                    .map(TwelveDataCandles::getValues)
                    .orElse(Collections.emptyList());
            candles.forEach(candle -> candle.setInterval(interval.getInterval()));
            dbUpdateService.saveDailyCandle(candles.stream()
                    .map(candle -> candlesMapper.toDbCandle(candle, ticker))
                    .collect(Collectors.toSet()));
        } catch (FeignException e) {
            log.error("Failed to retrieve candles for ticker {}, status {}", ticker, e.status());
            candles = Collections.emptyList();
        }
        return candles;
    }
}
