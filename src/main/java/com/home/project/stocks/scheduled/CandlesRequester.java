package com.home.project.stocks.scheduled;

import com.home.project.stocks.client.TinkoffClient;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.candles.Instrument;
import com.home.project.stocks.model.candles.Interval;
import com.home.project.stocks.model.candles.Payload;
import com.home.project.stocks.model.candles.StockByTicker;
import com.home.project.stocks.processor.StockProcessor;
import com.home.project.stocks.repository.StocksToScanRepository;
import com.home.project.stocks.service.IndicatorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Class that starts processing on schedule
 */
@Component
@Log4j2
public class CandlesRequester implements ScheduledRequester {

    private final TinkoffClient tinkoffClient;
    private final StocksToScanRepository stocksToScanRepository;

    @Autowired
    public CandlesRequester(TinkoffClient tinkoffClient,
                            StocksToScanRepository stocksToScanRepository) {
        this.tinkoffClient = tinkoffClient;
        this.stocksToScanRepository = stocksToScanRepository;
    }

    @Override
    @Scheduled(cron = "0 0 18 * * MON-FRI")
    @Profile("!test")
    public void requestData() {
        var stocks = stocksToScanRepository.findAll();

        stocks.forEach(stock -> {
            log.info(String.format("Start processing stock, ticker %s", stock.getTicker()));

            var candles = Optional.ofNullable(
                    tinkoffClient.getCandlesByFigi(stock.getFigi(), "", "", Interval.ONE_DAY.getPeriod()))
                    .map(StockByTicker::getPayload)
                    .map(Payload::getCandles)
                    .stream().findFirst()
                    .orElse(List.of());
//            stockProcessors.forEach(stockProcessor -> stockProcessor.processStock(stock.getTicker(), stock.getFigi(),
//                    candles));
        });
    }
}
