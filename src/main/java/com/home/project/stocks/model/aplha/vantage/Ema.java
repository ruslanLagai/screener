package com.home.project.stocks.model.aplha.vantage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class Ema {

    @JsonProperty("Meta Data")
    private Metadata metadata;
    @JsonProperty("Technical Analysis: EMA")
    private Map<String, EmaData> dates;

    @Data
    public static class EmaData {
        @JsonProperty("EMA")
        private String ema;
    }
}
