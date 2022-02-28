package com.home.project.stocks.model.candles;

import com.home.project.stocks.model.aplha.vantage.Metadata;
import lombok.Data;

import java.util.List;

@Data
public class TwelveDataCandles {
    protected Metadata meta;
    private List<Candle> values;
}
