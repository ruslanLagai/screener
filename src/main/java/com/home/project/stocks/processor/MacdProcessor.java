package com.home.project.stocks.processor;

import com.home.project.stocks.model.aplha.vantage.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.home.project.stocks.model.indicators.ParsedIndicator.*;

/**
 * Processor for MACD indicator
 * <p>
 * sing as ascending if:
 * - hist asc over last 3 days
 * - MACD crosses signal line above
 * sign as descending if:
 * - hist desc over last 3 days
 * - MACD crosses signal line below
 * <p>
 * Note:
 * - barValues, macdValues contain last 3 values starting from the latest one,
 * i.e. latest value at index 0
 */
@Component
@Log4j2
public class MacdProcessor implements IndicatorProcessor {

    private List<Double> barValues;
    private List<Double> macdValues;
    private List<Double> signalLineValues;
    private List<Date> lastDates;

    @Value("${indicator.macd.columnsNumber}")
    private int columnsNumber;

    @Override
    public void processIndicator(ParsedIndicator indicator, Candle candle, ProcessingResult processingResult) {
        extractLastDates(indicator);
        extractLastValues(indicator);
        processingResult.setMacdBarValues(barValues);
        processingResult.setMacdBarTrend(checkBarCondition());
        processingResult.setMacdSignalTrend(checkMacdSignal());
    }

    private void extractLastDates(ParsedIndicator indicator) {
        lastDates = indicator.getMacdData().keySet().stream()
                .sorted(Comparator.reverseOrder())
                .limit(columnsNumber)
                .collect(Collectors.toList());
    }

    private void extractLastValues(ParsedIndicator indicator) {
        barValues = lastDates.stream().map(date -> indicator.getMacdData().get(date).get(MACD_HIST))
                .collect(Collectors.toUnmodifiableList());
        macdValues = lastDates.stream().map(date -> indicator.getMacdData().get(date).get(MACD))
                .collect(Collectors.toUnmodifiableList());
        signalLineValues = lastDates.stream().map(date -> indicator.getMacdData().get(date).get(MACD_SIGNAL))
                .collect(Collectors.toUnmodifiableList());
    }

    private ProcessingResult.Trend checkBarCondition() {
        ProcessingResult.Trend trend = null;
        if (barValues.size() < columnsNumber) {
            return ProcessingResult.Trend.NO_SIGN;
        }
        for (int i = 1; i < barValues.size(); i++) {
            var current = barValues.get(i);
            var next = barValues.get(i - 1);
            trend = current > next && (trend == ProcessingResult.Trend.DESCENDING || trend == null)
                    ? ProcessingResult.Trend.DESCENDING :
                    current < next && (trend == ProcessingResult.Trend.ASCENDING || trend == null)
                            ? ProcessingResult.Trend.ASCENDING : ProcessingResult.Trend.NO_SIGN;
        }
        return trend;
    }

    private ProcessingResult.Trend checkMacdSignal() {
        ProcessingResult.Trend trend = null;
        if (macdValues.size() < columnsNumber) {
            return ProcessingResult.Trend.NO_SIGN;
        }

        boolean isCrossedAbove = false;
        boolean isCrossedBelow = false;

        for (int i = 0; i < macdValues.size() - 2; i++) {
            isCrossedAbove = macdValues.get(i + 2) <= signalLineValues.get(i + 2)
                    && (macdValues.get(i + 1) >= signalLineValues.get(i + 1)
                    || macdValues.get(i) >= signalLineValues.get(i));
            isCrossedBelow = macdValues.get(i + 2) >= signalLineValues.get(i + 2)
                    && (macdValues.get(i + 1) <= signalLineValues.get(i + 1)
                    || macdValues.get(i) <= signalLineValues.get(i));
        }
        return isCrossedAbove ? ProcessingResult.Trend.ASCENDING
                : isCrossedBelow ? ProcessingResult.Trend.DESCENDING
                : ProcessingResult.Trend.NO_SIGN;
    }
}
