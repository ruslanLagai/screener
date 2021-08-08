package com.home.project.stocks.model.indicators;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class ParsedIndicator {
    public static final String MACD = "MACD";
    public static final String MACD_HIST = "MACD_Hist";
    public static final String MACD_SIGNAL = "MACD_Signal";

    private Map<Date, Double> indicatorData;
    private Map<Date, Map<String, Double>> macdData;
    private String ticker;
    private String interval;
}
