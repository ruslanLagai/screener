package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;

public class AbstractProcessorTest {

    protected static final String FIGI = "figi";
    protected static final String TICKER = "ticker";

    protected static Candle generateCandle(double open, double close, double max, double min,
                                         double volume) {
        var candle = new Candle();
        candle.setC(close);
        candle.setO(open);
        candle.setFigi(FIGI);
        candle.setH(max);
        candle.setL(min);
        candle.setV(volume);
        return candle;
    }
}
