package com.home.project.stocks.processor;


import com.home.project.stocks.model.candles.Candle;

import java.time.LocalDateTime;

public class AbstractProcessorTest {

    protected static final String FIGI = "figi";
    protected static final String TICKER = "ticker";

    protected static Candle generateCandle(double open, double close, double max, double min,
                                           double volume, LocalDateTime localDateTime) {
        var candle = new Candle();
        candle.setC(close);
        candle.setO(open);
        candle.setH(max);
        candle.setL(min);
        candle.setV(volume);
        candle.setInterval("1day");
        candle.setDatetime(localDateTime);
        return candle;
    }
}
