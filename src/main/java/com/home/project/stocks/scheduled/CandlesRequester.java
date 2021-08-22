package com.home.project.stocks.scheduled;

import com.home.project.stocks.client.TinkoffRestClient;
import com.home.project.stocks.exceptions.TinkoffServerException;
import com.home.project.stocks.model.candles.Instrument;
import com.home.project.stocks.model.candles.Interval;
import com.home.project.stocks.model.candles.Payload;
import com.home.project.stocks.processor.StockProcessor;
import com.home.project.stocks.validator.CandleValidator;
import lombok.extern.log4j.Log4j2;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class that starts processing on schedule
 */
@Component
@Log4j2
public class CandlesRequester implements ScheduledRequester{

    private final TinkoffRestClient client;
    private final StockProcessor stockProcessor;

    @Autowired
    public CandlesRequester(TinkoffRestClient client,
                            StockProcessor stockProcessor) {
        this.client = client;
        this.stockProcessor = stockProcessor;
    }

    @Override
    @Scheduled(cron = "0 0 18 * * MON-FRI")
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
            var responseEntity = client.getCandles(instrument.getFigi(),
                    DateTime.now().minusDays(1), DateTime.now(), Interval.ONE_DAY);
            var candlesByFigi = Stream.of(responseEntity)
                    .filter(r -> r.getBody() != null && r.getBody().getPayload() != null)
                    .map(r -> r.getBody().getPayload())
                    .collect(Collectors.toMap(Payload::getFigi, Payload::getCandles));
            CandleValidator.removeInvalid(candlesByFigi);

            stockProcessor.processStock(instrument.getTicker(), instrument.getFigi(),
                    candlesByFigi.get(instrument.getFigi()));
        });
    }
}
