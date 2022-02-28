package com.home.project.stocks.model.indicators;

import com.home.project.stocks.model.entity.DailyEma;
import com.home.project.stocks.model.entity.DailyMacd;
import com.home.project.stocks.model.entity.DailyMom;
import com.home.project.stocks.model.entity.DailyRsi;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ParsedIndicator {
    public static final String MACD = "MACD";
    public static final String MACD_HIST = "MACD_Hist";
    public static final String MACD_SIGNAL = "MACD_Signal";

    private Map<Date, Double> indicatorData;
    private Map<Date, Map<String, Double>> macdData;
    private final String ticker;
    private final String interval;
    private final List<DailyEma> ema;
    private final List<DailyRsi> rsi;
    private final List<DailyMom> mom;
    private final List<DailyMacd> macd;
}
