package com.home.project.stocks.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author rlagay
 */
@Data
public class IndicatorMetadata {
    private String name;
    @JsonProperty("fast_period")
    private int fastPeriod;
    @JsonProperty("slow_period")
    private int slowPeriod;
    @JsonProperty("signal_period")
    private int signalPeriod;
    @JsonProperty("series_type")
    private String seriesType;
    @JsonProperty("time_period")
    private int timePeriod;
}
