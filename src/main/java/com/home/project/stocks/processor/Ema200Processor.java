package com.home.project.stocks.processor;

import com.home.project.stocks.model.aplha.vantage.EmaPeriod;
import org.springframework.stereotype.Component;

@Component
public class Ema200Processor extends EmaProcessor {

    public Ema200Processor() {
        emaPeriod = EmaPeriod.TWO_HUNDRED;
    }
}
