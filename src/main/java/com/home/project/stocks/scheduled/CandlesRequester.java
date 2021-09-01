package com.home.project.stocks.scheduled;

import com.home.project.stocks.client.TinkoffRestClient;
import com.home.project.stocks.exceptions.TinkoffServerException;
import com.home.project.stocks.model.candles.Instrument;
import com.home.project.stocks.model.candles.Payload;
import com.home.project.stocks.processor.StockProcessor;
import com.home.project.stocks.service.AlphaVantageService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Class that starts processing on schedule
 */
@Component
@Log4j2
public class CandlesRequester implements ScheduledRequester{

    private final TinkoffRestClient client;
    private final StockProcessor stockProcessor;
    private final AlphaVantageService alphaVantageService;

    @Autowired
    public CandlesRequester(TinkoffRestClient client,
                            StockProcessor stockProcessor,
                            AlphaVantageService alphaVantageService) {
        this.client = client;
        this.stockProcessor = stockProcessor;
        this.alphaVantageService = alphaVantageService;
    }

    @Override
    @Scheduled(cron = "0 0 18 * * MON-FRI")
    @Profile("!test")
    @Retryable(value = {TinkoffServerException.class}, maxAttempts = 2, backoff = @Backoff(delay = 1000))
    public void requestData() {
        var response = client.getStocks();
        var instruments = Stream.of(response)
                .filter(r -> r.getBody() != null && r.getBody().getPayload() != null)
                .map(r -> r.getBody().getPayload())
                .map(Payload::getInstruments)
                .findFirst()
                .orElse(new Instrument[]{});

        log.info("Number of received stocks: {}", instruments.length);
        Arrays.asList(instruments).forEach(instrument -> {
            log.info(String.format("Start processing stock, ticker %s", instrument.getTicker()));
            var candles = alphaVantageService.getDailyCandles(instrument.getTicker());

            stockProcessor.processStock(instrument.getTicker(), instrument.getFigi(),
                    candles.getCandles());
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        });
    }
}
