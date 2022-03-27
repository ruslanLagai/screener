package com.home.project.stocks.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.home.project.stocks.model.indicators.Indicator;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonIndicator {
    @JsonProperty("Meta Data")
    @JsonAlias("meta")
    protected Metadata metadata;
    @JsonProperty("Technical Analysis: EMA")
    @JsonAlias(value = {"Technical Analysis: RSI", "Technical Analysis: MACD"})
    private Map<String, IndicatorData> dates;
    private List<Indicator> values;

    @Data
    public static class IndicatorData {
        @JsonProperty("EMA")
        @JsonAlias("RSI")
        private Double indicator;
        @JsonProperty("MACD")
        private Double macd;
        @JsonProperty("MACD_Hist")
        private Double macdHist;
        @JsonProperty("MACD_Signal")
        private Double macdSignal;
    }
}
