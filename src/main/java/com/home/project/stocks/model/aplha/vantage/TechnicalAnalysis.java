package com.home.project.stocks.model.aplha.vantage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TechnicalAnalysis {

    @JsonProperty("EMA")
    private Ema ema;
}
