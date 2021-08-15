package com.home.project.stocks.processor;

import com.home.project.stocks.model.aplha.vantage.EmaPeriod;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

/**
 * Class to process EMA
 */
@Log4j2
public abstract class EmaProcessor implements IndicatorProcessor {

    @Value("${indicator.ema.threshold}")
    protected double threshold;

    protected EmaPeriod emaPeriod;

    @Override
    public void processIndicator(ParsedIndicator indicator, Candle candle, ProcessingResult processingResult) {
        var lastDate = indicator.getIndicatorData().keySet().stream().max(Date::compareTo).orElse(null);
        var emaValue = indicator.getIndicatorData().get(lastDate);
        var isSupportLevel = isSupportLevel(candle.getH(), emaValue);
        var difference = calculateDifference(isSupportLevel ? candle.getL() : candle.getH(), emaValue);
        processingResult.getEmaValue().put(this.emaPeriod, initEmaData(emaValue, difference, isCloseToEma(difference),
                isSupportLevel ? ProcessingResult.LevelType.SUPPORT
                        : ProcessingResult.LevelType.RESISTANCE));
    }

    /**
     * Calculate distance to ema in %
     * if price is higher > 1
     * if price is lower < 1
     * @param price - stock price
     * @param ema - ema
     * @return - percentage
     */
    protected double calculateDifference(double price, double ema) {
        return ema / price;
    }

    protected boolean isSupportLevel(double minPrice, double emaValue) {
        return minPrice >= emaValue;
    }

    protected boolean isResistanceLevel(double maxPrice, double emaValue) {
        return maxPrice <= emaValue;
    }

    protected boolean isCloseToEma(double difference) {
        var range = Range.between(1 - threshold, 1 + threshold);
        return range.contains(difference);
    }

    protected ProcessingResult.EmaData initEmaData(double emaValue, double difference, boolean isClose,
                                                   ProcessingResult.LevelType levelType) {
        return ProcessingResult.EmaData.builder()
                .emaValue(emaValue)
                .difference(difference)
                .isCloseToEma(isClose)
                .levelType(levelType)
                .build();

    }

}
