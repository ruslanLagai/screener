package com.home.project.stocks.model.candles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.home.project.stocks.model.Currency;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Instrument {
    private String figi;
    private String ticker;
    private double minPriceIncrement;
    private int lot;
    private Currency currency;
    private String name;
    private String type;
}
