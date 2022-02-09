package com.home.project.stocks.parser;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.home.project.stocks.model.indicators.ParsedIndicator.*;

import com.home.project.stocks.exceptions.IndicatorParsingException;
import com.home.project.stocks.model.aplha.vantage.CommonIndicator;
import com.home.project.stocks.model.indicators.ParsedIndicator;

import com.home.project.stocks.utils.DateTimeParser;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;

/**
 * Parse response
 */
@Log4j2
public class AlphaVantageParser {

    public static ParsedIndicator parseIndicator(CommonIndicator indicator) {
        Map<Date, Double> data = new HashMap<>();
        if (CollectionUtils.isEmpty(indicator.getDates())) {
            log.warn("Empty indicator data for {}, indicator {}",
                    indicator.getMetadata() != null ? indicator.getMetadata().getSymbol() : null,
                    indicator.getMetadata() != null ? indicator.getMetadata().getIndicator() : null);
            return null;
        }
        parseDate(indicator.getDates()).forEach((k, v) -> extractData(data, k, v));
        return new ParsedIndicator(data, null, indicator.getMetadata().getSymbol(),
                indicator.getMetadata().getInterval());
    }

    public static ParsedIndicator parseMacd(CommonIndicator indicator) {
        Map<Date, Map<String, Double>> data = new HashMap<>();
        try {
            if (CollectionUtils.isEmpty(indicator.getDates())) {
                log.warn("Empty indicator data for {}, indicator: macd",
                        indicator.getMetadata() != null ? indicator.getMetadata().getSymbol() : null);
                return null;
            }
            parseDate(indicator.getDates()).forEach((k, v) -> extractMacdData(data, k, v));
        } catch (IndicatorParsingException e) {
            log.error("Failed to process MACD for " + indicator.getMetadata().getSymbol());
        }
        return new ParsedIndicator(null, data, indicator.getMetadata().getSymbol(),
                indicator.getMetadata().getInterval());
    }

    private static Map<Date, CommonIndicator.IndicatorData> parseDate(Map<String, CommonIndicator.IndicatorData> data) {
        Map<Date, CommonIndicator.IndicatorData> map = new HashMap<>();
        if (CollectionUtils.isEmpty(data)) {
            log.warn("Empty indicator data");
            return map;
        }
        data.forEach((k, v) -> {
            var date = DateTimeParser.parseDate(k);
            map.put(date, v);
        });
        return map;
    }

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
