package com.home.project.stocks.parser;

import java.time.Instant;
import java.time.Period;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.home.project.stocks.model.indicators.ParsedIndicator.*;

import com.home.project.stocks.exceptions.IndicatorParsingException;
import com.home.project.stocks.model.aplha.vantage.CommonIndicator;
import lombok.extern.slf4j.Slf4j;

/**
 * Parse response
 */
@Slf4j
public class AlphaVantageParser {

    private static void extractData(Map<Date, Double> data, Date date,
                                    CommonIndicator.IndicatorData indicatorData) {
        if (date.after(Date.from(Instant.now().minus(Period.ofDays(2000))))) {
            data.put(date, indicatorData.getIndicator());
        }
    }

    private static void extractMacdData(Map<Date, Map<String, Double>> data, Date date,
                                        CommonIndicator.IndicatorData indicatorData) {
        Map<String, Double> macdData = new HashMap<>();
        if (indicatorData.getMacd() == null || indicatorData.getMacdHist() == null
                || indicatorData.getMacdSignal() == null) {
            throw new IndicatorParsingException("Not enough data for MACD indicator");
        }
        macdData.put(MACD, indicatorData.getMacd());
        macdData.put(MACD_HIST, indicatorData.getMacdHist());
        macdData.put(MACD_SIGNAL, indicatorData.getMacdSignal());
        data.put(date, macdData);
    }
}
