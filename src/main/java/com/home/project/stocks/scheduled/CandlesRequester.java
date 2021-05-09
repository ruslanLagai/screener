package com.home.project.stocks.scheduled;

import com.home.project.stocks.model.Payload;
import com.home.project.stocks.processor.PatternOrchestration;
import com.home.project.stocks.processor.StocksProcessor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.home.project.stocks.client.TinkoffRestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    @Scheduled(cron = "00 1 ? * MON-FRI")
    public void requestData() {
        var response = client.getStocks();
        var candlesByFigi = Stream.of(response)
                .filter(r -> r.getBody() != null && r.getBody().getPayload() != null)
                .map(r -> r.getBody().getPayload())
                .collect(Collectors.toMap(Payload::getFigi, Payload::getCandles));
        log.info("Number of received stocks: {}", candlesByFigi.size());
        orchestration.processStocks(candlesByFigi);
    }
}
