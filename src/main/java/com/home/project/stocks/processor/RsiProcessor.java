package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        var lastDates = indicator.getIndicatorData().keySet().stream()
                .sorted(Comparator.reverseOrder())
                .limit(columnsNumber)
                .collect(Collectors.toList());
        var lastValues = lastDates.stream()
                .map(date -> indicator.getIndicatorData().get(date))
                .collect(Collectors.toUnmodifiableList());
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
