package com.home.project.stocks.model.aplha.vantage;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * Class to represent alpha vantage candles
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Candles {

    @JsonProperty("Meta Data")
    protected Metadata metadata;
    @JsonProperty("Time Series (Daily)")
    @JsonAlias(value = {"Weekly Adjusted Time Series"})
    private Map<Date, Candle> candles;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        @JsonProperty("1. Information")
        private String information;
        @JsonProperty("2. Symbol")
        private String symbol;
        @JsonProperty("3. Last Refreshed")
        private Date lastRefreshed;
        @JsonProperty("4. Output Size")
        private String outputSize;
        @JsonProperty("5. Time Zone")
        private String timeZone;
    }

}
