package com.home.project.stocks.processor;

import com.home.project.stocks.model.api.EmaPeriod;
import com.home.project.stocks.service.CandlesService;
import org.springframework.stereotype.Component;

@Component
public class Ema1000Processor extends EmaProcessor {

    public Ema1000Processor(CandlesService candlesService) {
        super(candlesService);
        emaPeriod = EmaPeriod.ONE_THOUSAND;
    }
}
