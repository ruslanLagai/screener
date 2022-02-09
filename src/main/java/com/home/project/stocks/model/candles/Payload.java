package com.home.project.stocks.model.candles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Payload {
    private int total;
    private String figi;
    private String ticker;
    private String isin;
    //todo change to enum
    private String interval;
    private Instrument[] instruments;
    private List<Candle> candles;
}
