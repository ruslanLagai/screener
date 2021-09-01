package com.home.project.stocks.processor;


import com.home.project.stocks.model.aplha.vantage.Candle;

public class AbstractProcessorTest {

    protected static final String FIGI = "figi";
    protected static final String TICKER = "ticker";

    protected static Candle generateCandle(double open, double close, double max, double min,
                                           double volume) {
        var candle = new Candle();
        candle.setClose(close);
        candle.setOpen(open);
        candle.setHigh(max);
        candle.setLow(min);
        candle.setVolume(volume);
        return candle;
    }
}
