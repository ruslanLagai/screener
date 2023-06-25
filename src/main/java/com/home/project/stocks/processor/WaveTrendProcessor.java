package com.home.project.stocks.processor;

import com.home.project.stocks.indicators.ExponentialMovingAverage;
import com.home.project.stocks.indicators.SimpleMovingAverage;
import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.service.CandlesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.Precision;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;

/**
 * buy sign when wt1 crosses wt2 up && lines below osLevel1
 *
 * short is disabled
 *
 * @author rlagay
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WaveTrendProcessor implements IndicatorProcessor {

    private static final double obLevel1 = 60;
    private static final double obLevel2 = 53;
    private static final double osLevel1 = -60;
    private static final double osLevel2 = -53;


    private final ExponentialMovingAverage ema = new ExponentialMovingAverage();
    private final SimpleMovingAverage sma = new SimpleMovingAverage();

    private final CandlesService candlesService;

    @Override
    public void processIndicator(ParsedIndicator indicator, Candle candle, ProcessingResult processingResult) {
        var calculatedWT = calculateIndicator(indicator.getTicker());
        if (calculatedWT == null) {
            return;
        }
        var isOversold = isBelowLowerLevel(calculatedWT);
        var isCrossesUp = isCrossesUp(calculatedWT);
        if (isOversold && isCrossesUp) {
            processingResult.setWtTrend(ProcessingResult.Trend.ASCENDING);
        } else {
            processingResult.setWtTrend(ProcessingResult.Trend.NO_SIGN);
        }
    }

    private boolean isCrossesUp(Pair<double[], double[]> wtValues) {
        var wt1 = wtValues.getLeft()[wtValues.getLeft().length - 1];
        var wt2 = wtValues.getRight()[wtValues.getRight().length - 1];
        var wt1Prev =  wtValues.getLeft()[wtValues.getLeft().length - 2];
        var wt2Prev =  wtValues.getRight()[wtValues.getRight().length - 2];

        return wt1 >= wt2 && wt1Prev < wt2Prev;
    }

    private boolean isBelowLowerLevel(Pair<double[], double[]> wtValues) {
        var wt1 = wtValues.getLeft()[wtValues.getLeft().length - 1];
        var wt2 = wtValues.getRight()[wtValues.getRight().length - 1];
        return wt1 < osLevel2 && wt2 < osLevel2;
    }

    private Pair<double[], double[]> calculateIndicator(String ticker) {
        var candles = candlesService.getHistoricalCandles(ticker, Interval.TWELVE_DATA_ONE_DAY, 45)
            .stream()
            .sorted(Comparator.comparing(Candle::getDatetime, Comparator.naturalOrder()))
            .toList();
        try {
            var averageHLC = candles.stream()
                .map(c -> (Precision.round(c.getH(), 2) + Precision.round(c.getL(), 2) + Precision.round(c.getC(), 2)) / 3)
                .toList().stream().mapToDouble(Double::doubleValue).toArray();
            var esa = Arrays.stream(ema.calculate(averageHLC, 10).getEMA()).toArray();
            double[] list = new double[averageHLC.length];
            for (int i = 0; i < averageHLC.length; i++) {
                list[i] = Math.abs(averageHLC[i] - esa[i]);
            }
            var d = ema.calculate(list,10).getEMA();
            double[] ci = new double[averageHLC.length];
            for (int i = 0; i < averageHLC.length; i++) {
                ci[i] = (averageHLC[i] - esa[i]) / (0.015 * d[i]);
            }
            ci = Arrays.stream(ci).filter(Double::isFinite).toArray();
            var tci = ema.calculate(ci, 21);

            var wt1 = tci.getEMA();
            var wt2 = sma.calculate(wt1, 4).getSMA();
            return Pair.of(wt1, wt2);
        } catch (Exception e) {
            log.error("Failed to calculate wave trend indicator, ticker {}", ticker, e);
        }
        return null;
    }
}
