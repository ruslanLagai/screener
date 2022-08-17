package com.home.project.stocks.parser;

import com.home.project.stocks.exceptions.IndicatorParsingException;
import com.home.project.stocks.model.api.CommonIndicator;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.model.entity.DailyIndicator;
import com.home.project.stocks.model.entity.DailyEma;
import com.home.project.stocks.model.entity.DailyMacd;
import com.home.project.stocks.model.entity.DailyRsi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Parse response from twelve data api
 */
@Slf4j
public class TwelveDataParser {

    public static ParsedIndicator convertToParsedIndicator(DailyIndicator indicator) {
        return ParsedIndicator.builder()
                .interval(indicator.getTimeframe())
                .ema(indicator.getEmaData() == null ? null : List.copyOf(indicator.getEmaData()))
                .rsi(indicator.getRsiData() == null ? null : List.copyOf(indicator.getRsiData()))
                .macd(indicator.getMacdData() == null ? null : List.copyOf(indicator.getMacdData()))
                .mom(indicator.getMomData() == null ? null : List.copyOf(indicator.getMomData()))
                .ticker(indicator.getTicker())
                .build();
    }

    public static DailyIndicator parseEma(CommonIndicator indicator) {
        if (validateNotNull(indicator)) {
            throw new IndicatorParsingException("No data to parse");
        }
        var dailyIndicator = DailyIndicator.builder()
                .date(indicator.getValues().iterator().next().getDatetime())
                .ticker(indicator.getMetadata().getSymbol())
                .timeframe(indicator.getMetadata().getInterval())
                .emaData(indicator.getValues().stream()
                        .map(item -> DailyEma.builder()
                                .emaValue(item.getEma())
                                .emaType(String.valueOf(indicator.getMetadata().getIndicatorMetadataData().getTimePeriod()))
                                .datetime(item.getDatetime())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
        dailyIndicator.getEmaData().forEach(dailyEma -> dailyEma.setDailyIndicator(dailyIndicator));
        return dailyIndicator;
    }

    public static DailyIndicator parseRsi(CommonIndicator indicator) {
        if (validateNotNull(indicator)) {
            throw new IndicatorParsingException("No data to parse");
        }
        var dailyIndicator = DailyIndicator.builder()
                .date(indicator.getValues().iterator().next().getDatetime())
                .ticker(indicator.getMetadata().getSymbol())
                .timeframe(indicator.getMetadata().getInterval())
                .rsiData(indicator.getValues().stream()
                        .map(item -> DailyRsi.builder()
                                .rsiValue(item.getRsi())
                                .datetime(item.getDatetime())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
        dailyIndicator.getRsiData().forEach(rsi -> rsi.setDailyIndicator(dailyIndicator));
        return dailyIndicator;
    }

    public static DailyIndicator parseMacd(CommonIndicator indicator) {
        if (validateNotNull(indicator)) {
            throw new IndicatorParsingException("No data to parse");
        }
        var dailyIndicator = DailyIndicator.builder()
                .date(indicator.getValues().iterator().next().getDatetime())
                .ticker(indicator.getMetadata().getSymbol())
                .timeframe(indicator.getMetadata().getInterval())
                .macdData(indicator.getValues().stream()
                        .map(item -> DailyMacd.builder()
                                .macdValue(item.getMacd())
                                .macdHistValue(item.getMacdHist())
                                .macdSignalValue(item.getMacdSignal())
                                .datetime(item.getDatetime())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
        dailyIndicator.getMacdData().forEach(macd -> macd.setDailyIndicator(dailyIndicator));
        return dailyIndicator;
    }

    private static boolean validateNotNull(CommonIndicator indicator) {
        if (CollectionUtils.isEmpty(indicator.getValues())) {
            log.warn("Empty indicator data for {}, indicator {}",
                    indicator.getMetadata() != null ? indicator.getMetadata().getSymbol() : null,
                    indicator.getMetadata() != null ? indicator.getMetadata().getIndicator() : null);
            return true;
        }
        return false;
    }
}
