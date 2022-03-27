package com.home.project.stocks.processor;

import com.home.project.stocks.model.api.EmaPeriod;
import com.home.project.stocks.service.CandlesService;
import org.springframework.stereotype.Component;

@Component
public class Ema200Processor extends EmaProcessor {

    public Ema200Processor(CandlesService candlesService) {
        super(candlesService);
        emaPeriod = EmaPeriod.TWO_HUNDRED;
    }
}
