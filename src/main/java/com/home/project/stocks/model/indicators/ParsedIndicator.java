package com.home.project.stocks.model.indicators;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class ParsedIndicator {
    private Map<Date, Double> indicatorData;
    private Map<Date, Map<String, Double>> macdData;
    private String ticker;
    private String interval;
}
