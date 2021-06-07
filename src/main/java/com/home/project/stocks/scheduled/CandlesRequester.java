package com.home.project.stocks.scheduled;

import com.home.project.stocks.exceptions.TinkoffServerException;
import com.home.project.stocks.model.candles.Payload;
import com.home.project.stocks.processor.PatternOrchestration;
import com.home.project.stocks.validator.CandleValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.home.project.stocks.client.TinkoffRestClient;
import org.springframework.web.server.ServerErrorException;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Log4j2
public class CandlesRequester implements ScheduledRequester{

    private TinkoffRestClient client;
    private PatternOrchestration orchestration;

    @Autowired
    public void setOrchestration(PatternOrchestration orchestration) {
        this.orchestration = orchestration;
    }

    @Autowired
    public void setClient(TinkoffRestClient client) {
        this.client = client;
    }

    @Override
    @Scheduled(cron = "0 0 18 * * MON-FRI")
    @Retryable(value = {TinkoffServerException.class}, maxAttempts = 2, backoff = @Backoff(delay = 1000))
    public void requestData() {
        var response = client.getStocks();
        var candlesByFigi = Stream.of(response)
                .filter(r -> r.getBody() != null && r.getBody().getPayload() != null)
                .map(r -> r.getBody().getPayload())
                .collect(Collectors.toMap(Payload::getFigi, Payload::getCandles));
        log.info("Number of received stocks: {}", candlesByFigi.size());
        CandleValidator.removeInvalid(candlesByFigi);
        orchestration.processStocks(candlesByFigi);
    }
}
