package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.entity.DailyRsi;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.model.processing.ProcessingResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Processor for RSI oversold/overbought indicator
 */
@Component
@Log4j2
public class RsiProcessor implements IndicatorProcessor {

    private static final double RSI_LOWER_LIMIT = 30;
    private static final double RSI_UPPER_LIMIT = 70;

    @Value("${indicator.rsi.columnsNumber}")
    private int columnsNumber;

    @Override
    public void processIndicator(ParsedIndicator indicator, Candle candle, ProcessingResult processingResult) {
        var lastRsi = indicator.getRsi().stream()
            .sorted(Comparator.comparing(DailyRsi::getDatetime, Comparator.reverseOrder()))
            .limit(columnsNumber).toList();
        var lastValues = lastRsi.stream()
            .map(DailyRsi::getRsiValue).toList();
        processingResult.setRsiSign(checkCondition(lastValues));
        processingResult.setRsiValues(lastValues);
    }

    private static ProcessingResult.RsiSign checkCondition(List<Double> values) {
        var isOversold = values.stream().allMatch(value -> value <= RSI_LOWER_LIMIT);
        var isOverbought = values.stream().allMatch(value -> value >= RSI_UPPER_LIMIT);
        return isOversold ? ProcessingResult.RsiSign.OVERSOLD
                : isOverbought ? ProcessingResult.RsiSign.OVERBOUGHT
                : ProcessingResult.RsiSign.NO_SIGN;
    }
}
