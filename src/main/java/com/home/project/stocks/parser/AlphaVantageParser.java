package com.home.project.stocks.parser;

import com.home.project.stocks.exceptions.IndicatorParsingException;
import com.home.project.stocks.model.aplha.vantage.CommonIndicator;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.home.project.stocks.model.indicators.ParsedIndicator.*;
import static com.home.project.stocks.utils.DateTimeParser.parseDate;

@Log4j2
public class AlphaVantageParser {

    public static ParsedIndicator parseIndicator(CommonIndicator indicator) {
        Map<Date, Double> data = new HashMap<>();
        indicator.getDates().forEach((k, v) -> extractData(data, k, v));
        return new ParsedIndicator(data, null, indicator.getMetadata().getSymbol(),
                indicator.getMetadata().getInterval());
    }

    public static ParsedIndicator parseMacd(CommonIndicator indicator) {
        Map<Date, Map<String, Double>> data = new HashMap<>();
        try {
            indicator.getDates().forEach((k, v) -> extractMacdData(data, k, v));
        } catch (IndicatorParsingException e) {
            log.error("Failed to process MACD for " + indicator.getMetadata().getSymbol());
        }
        return new ParsedIndicator(null, data, indicator.getMetadata().getSymbol(),
                indicator.getMetadata().getInterval());
    }

    private static void extractData(Map<Date, Double> data, String date,
                                      CommonIndicator.IndicatorData indicatorData) {
        var parsedDate = parseDate(date);
        if (parsedDate != null && parsedDate.after(Date.from(Instant.now().minus(Period.ofDays(14))))) {
            data.put(parseDate(date), indicatorData.getIndicator());
        }
    }

    private static void extractMacdData(Map<Date, Map<String, Double>> data, String date,
                                      CommonIndicator.IndicatorData indicatorData) {
        Map<String, Double> macdData = new HashMap<>();
        if (indicatorData.getMacd() == null || indicatorData.getMacdHist() == null
                || indicatorData.getMacdSignal() == null) {
            throw new IndicatorParsingException("Not enough data for MACD indicator");
        }
        macdData.put(MACD, indicatorData.getMacd());
        macdData.put(MACD_HIST, indicatorData.getMacdHist());
        macdData.put(MACD_SIGNAL, indicatorData.getMacdSignal());
        data.put(parseDate(date), macdData);
    }
}
