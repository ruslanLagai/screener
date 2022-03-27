package com.home.project.stocks.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata {
    @JsonProperty("1: Symbol")
    @JsonAlias("symbol")
    private String symbol;
    @JsonProperty("2: Indicator")
    private String indicator;
    @JsonProperty("4: Interval")
    @JsonAlias("interval")
    private String interval;
    @JsonProperty("5: Time Period")
    private int timePeriod;
    @JsonProperty("5.1: Fast Period")
    private int fastPeriod;
    @JsonProperty("5.2: Slow Period")
    private int slowPeriod;
    @JsonProperty("5.3: Signal Period")
    private int signalPeriod;
    @JsonProperty("6: Series Type")
    private String seriesType;
    @JsonProperty("7: Time Zone")
    private String timeZone;
    private String exchange;
    private String type;
    @JsonProperty("indicator")
    private IndicatorMetadata indicatorMetadataData;
}
