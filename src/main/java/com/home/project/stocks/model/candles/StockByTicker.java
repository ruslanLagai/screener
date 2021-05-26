package com.home.project.stocks.model.candles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.home.project.stocks.model.candles.Payload;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class StockByTicker {

    private String trackingId;
    private String status;
    private Payload payload;

}
