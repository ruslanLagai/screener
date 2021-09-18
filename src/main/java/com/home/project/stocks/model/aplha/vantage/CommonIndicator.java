package com.home.project.stocks.model.aplha.vantage;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class CommonIndicator {
    @JsonProperty("Meta Data")
    protected Metadata metadata;
    @JsonProperty("Technical Analysis: EMA")
    @JsonAlias(value = {"Technical Analysis: RSI", "Technical Analysis: MACD"})
    private Map<Date, IndicatorData> dates;

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
