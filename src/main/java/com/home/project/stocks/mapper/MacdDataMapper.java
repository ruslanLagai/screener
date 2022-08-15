package com.home.project.stocks.mapper;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.entity.DailyMacd;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.model.processing.MacdData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rlagay
 */
@Slf4j
@Component
public class MacdDataMapper {

    public List<MacdData> toMacdData(ParsedIndicator indicator, Collection<Candle> candles) {
        if (indicator.getMacd().size() > candles.size()) {
            log.warn("Number of macd more then candles, macd size {}, candles size {}",
                    indicator.getMacd().size(), candles.size());
        }
        return indicator.getMacd().stream()
                .sorted(Comparator.comparing(DailyMacd::getDatetime, Comparator.reverseOrder()))
                .map(macd -> {
                    var candle = candles.stream()
                            .filter(c -> c.getDatetime().equals(macd.getDatetime()))
                            .findFirst()
                            .orElse(null);
                    return MacdData.builder()
                            .macdBarValue(macd.getMacdHistValue())
                            .macdValue(macd.getMacdValue())
                            .macdSignalValue(macd.getMacdSignalValue())
                            .closePrice(candle != null ? candle.getC() : -1)
                            .dateTime(macd.getDatetime())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
