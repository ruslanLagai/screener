package com.home.project.stocks.model.aplha.vantage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata {
    @JsonProperty("1: Symbol")
    private String symbol;
    @JsonProperty("2: Indicator")
    private String indicator;
    @JsonProperty("4: Interval")
    private String interval;
    @JsonProperty("5: Time Period")
    private int timePeriod;
    @JsonProperty("6: Series Type")
    private String seriesType;
    @JsonProperty("7: Time Zone")
    private String timeZone;
}
