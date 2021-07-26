package com.home.project.stocks.processor;

import com.home.project.stocks.model.aplha.vantage.EmaPeriod;
import org.springframework.stereotype.Component;

@Component
public class Ema1000Processor extends EmaProcessor {

    public Ema1000Processor() {
        emaPeriod = EmaPeriod.ONE_THOUSAND;
    }
}
