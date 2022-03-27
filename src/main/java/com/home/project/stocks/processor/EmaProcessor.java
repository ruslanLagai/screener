package com.home.project.stocks.processor;

import com.home.project.stocks.model.api.EmaPeriod;
import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.entity.DailyEma;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.service.CandlesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;

/**
 * Class to process EMA
 */
@Slf4j
@RequiredArgsConstructor
public abstract class EmaProcessor implements IndicatorProcessor {

    @Value("${indicator.ema.threshold}")
    protected double threshold;

    protected EmaPeriod emaPeriod;
    protected final CandlesService candlesService;

    @Override
    public void processIndicator(ParsedIndicator indicator, Candle candle, ProcessingResult processingResult) {
        if (CollectionUtils.isEmpty(indicator.getEma())) {
            log.warn(String.format("Stock hasn't enough historical data to calculate %s ema, ticker %s",
                    emaPeriod.getPeriod(), indicator.getTicker()));
            return;
        }
        var candles = candlesService.getHistoricalCandles(indicator.getTicker(),
                Interval.parse(candle.getInterval()), 25);
        if (CollectionUtils.isEmpty(candles) || candles.size() < 25) {
            log.warn("Failed to retrieve historical candles for detecting close retest");
            return;
        }

        var emaValue = indicator.getEma().stream()
                .max(Comparator.comparing(DailyEma::getDatetime))
                .map(DailyEma::getEmaValue)
                .orElseThrow(() -> new IllegalArgumentException("Failed to process ema, unable to retrieve last ema"));

        var isSupportLevel = isSupportLevel(candle.getL(), emaValue);
        var difference = calculateDifference(isSupportLevel ? candle.getL() : candle.getH(), emaValue);
        var level = isSupportLevel ? ProcessingResult.LevelType.SUPPORT : ProcessingResult.LevelType.RESISTANCE;
        var isCloseRetest = isCloseRetest(candles, emaValue, isSupportLevel);
        processingResult.getEmaValue().put(this.emaPeriod,
                initEmaData(emaValue, difference, isCloseToEma(difference), level, isCloseRetest));
    }

    /**
     * Check close retest
     *
     * @param candles   candles for period
     * @param emaValue  ema value
     * @param isSupport is support level
     */
    protected boolean isCloseRetest(List<Candle> candles, double emaValue, boolean isSupport) {
        var extremum = isSupport
                ? candles.stream().min(Comparator.comparing(Candle::getL)).orElse(null)
                : candles.stream().max(Comparator.comparing(Candle::getH)).orElse(null);
        if (extremum == null) {
            log.warn("Failed to find extremum for long period");
            return false;
        }
        return isSupport
                ? extremum.getL() <= emaValue || isCloseToEma(extremum.getL())
                : extremum.getH() >= emaValue || isCloseToEma(extremum.getH());
    }

    /**
     * Calculate distance to ema in %
     *
     * @param price  stock price
     * @param ema    ema
     * @return       percentage
     */
    protected double calculateDifference(double price, double ema) {
        return Math.abs(ema - price) / price;
    }

    protected boolean isSupportLevel(double minPrice, double emaValue) {
        return minPrice >= emaValue;
    }

    protected boolean isResistanceLevel(double maxPrice, double emaValue) {
        return maxPrice <= emaValue;
    }

    protected boolean isCloseToEma(double difference) {
        var range = Range.between(0.0, threshold);
        return range.contains(difference);
    }

    protected ProcessingResult.EmaData initEmaData(double emaValue, double difference, boolean isClose,
                                                   ProcessingResult.LevelType levelType, boolean isCloseRetest) {
        return ProcessingResult.EmaData.builder()
                .emaValue(emaValue)
                .difference(difference)
                .isCloseToEma(isClose)
                .isCloseRetest(isCloseRetest)
                .levelType(levelType)
                .build();

    }

}
