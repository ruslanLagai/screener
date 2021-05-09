package com.home.project.stocks.model.candles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.home.project.stocks.model.Payload;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class CandlesByFigi {

    private String trackingId;
    private String status;
    private Payload payload;

}
