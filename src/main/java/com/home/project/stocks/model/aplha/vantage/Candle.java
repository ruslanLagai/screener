package com.home.project.stocks.model.aplha.vantage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Candle {
    @JsonProperty("1. open")
    private double open;
    @JsonProperty("2. high")
    private double high;
    @JsonProperty("3. low")
    private double low;
    @JsonProperty("4. close")
    private double close;
    @JsonProperty("5. adjusted close")
    private double adjustedClose;
    @JsonProperty("6. volume")
    private double volume;
    @JsonProperty("7. dividend amount")
    private double dividend;
    private Date date;
}
