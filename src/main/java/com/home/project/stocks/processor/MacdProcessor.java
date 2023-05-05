package com.home.project.stocks.processor;

import com.google.common.collect.Comparators;
import com.home.project.stocks.exceptions.MacdProcessingException;
import com.home.project.stocks.mapper.MacdDataMapper;
import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.MacdData;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.service.CandlesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.home.project.stocks.model.processing.ProcessingResult.Trend.ASCENDING;
import static com.home.project.stocks.model.processing.ProcessingResult.Trend.DESCENDING;
import static com.home.project.stocks.model.processing.ProcessingResult.Trend.NO_SIGN;
import static java.lang.Math.abs;

/**
 * Processor for MACD indicator
 * <p>
 * buy signal:
 * - MACD divergence: both have ascending bars
 * - MACD hist max > |15| (at least one)
 * - MACD crosses signal line above - TBD
 * sell signal:
 * - MACD divergence: both have descending bars
 * - MACD hist min < -15 (at least one)
 * - MACD crosses signal line below - TBD
 * <p>
 * Note:
 * - barValues, macdValues contain last 3 values starting from the latest one,
 * i.e. latest value at index 0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MacdProcessor implements IndicatorProcessor {

    private final CandlesService candlesService;
    private final MacdDataMapper macdDataMapper;

    @Value("${indicator.macd.columnsNumber}")
    private int columnsNumber;

    @Override
    public void processIndicator(ParsedIndicator indicator, Candle candle, ProcessingResult processingResult) {
        if (CollectionUtils.isEmpty(indicator.getMacd())) {
            log.warn("Empty or null macd data");
            return;
        }
        var candles = candlesService.getHistoricalCandles(processingResult.getTicker(),
                Interval.parseOrDefault(candle.getInterval(), Interval.TWELVE_DATA_ONE_DAY), 30);

        if (CollectionUtils.isEmpty(candles) || candles.size() < 25) {
            log.warn("Failed to retrieve historical candles for detecting macd divergence");
            return;
        }
        var macdData = macdDataMapper.toMacdData(indicator, candles);

        processingResult.setMacdBarTrend(checkBarCondition(macdData));
        processingResult.setMacdSignalTrend(checkMacdSignal(macdData));
        processingResult.setMacdDivergence(checkDivergence(macdData));
        if (processingResult.getClosePrice() == 0.0) {
            processingResult.setClosePrice(candles.get(0).getC());
        }
    }

    private ProcessingResult.Trend checkBarCondition(List<MacdData> macdData) {
        ProcessingResult.Trend trend = null;
        if (macdData.size() < columnsNumber) {
            log.warn("Not enough macdData to detect bar trend");
            return NO_SIGN;
        }
        for (int i = 0; i < columnsNumber; i++) {
            var recent = macdData.get(i).getMacdBarValue();
            var prev = macdData.get(i + 1).getMacdBarValue();
            trend = prev > recent && (trend == ProcessingResult.Trend.DESCENDING || trend == null)
                    ? ProcessingResult.Trend.DESCENDING :
                    prev < recent && (trend == ASCENDING || trend == null)
                            ? ASCENDING : NO_SIGN;
        }
        return trend;
    }

    private ProcessingResult.Trend checkMacdSignal(List<MacdData> macdData) {
        if (macdData.size() < columnsNumber) {
            log.warn("Not enough macdData to detect signal line cross");
            return NO_SIGN;
        }
        var lastValues = macdData.subList(0, columnsNumber);
        //исключение боковиков
        var isNotClear = lastValues.stream()
                .map(MacdData::getMacdBarValue)
                .allMatch(bar -> abs(bar) < 0.6);
        if (isNotClear) {
            return NO_SIGN;
        }

        var isMacdLineAsc = Comparators.isInOrder(lastValues, Comparator.comparing(MacdData::getMacdValue, Comparator.reverseOrder()));
        var isSignalLineDesc = Comparators.isInOrder(lastValues, Comparator.comparing(MacdData::getMacdSignalValue, Comparator.naturalOrder()));

        var isMacdLineDesc = Comparators.isInOrder(lastValues, Comparator.comparing(MacdData::getMacdValue, Comparator.naturalOrder()));
        var isSignalLineAsc = Comparators.isInOrder(lastValues, Comparator.comparing(MacdData::getMacdSignalValue, Comparator.reverseOrder()));

        return isMacdLineAsc && isSignalLineDesc ? ASCENDING
                : isMacdLineDesc && isSignalLineAsc ? ProcessingResult.Trend.DESCENDING
                : NO_SIGN;
    }

    private ProcessingResult.Trend checkDivergence(List<MacdData> macdData) {
        var latestValue = macdData.get(0).getMacdBarValue();
        return macdData.stream()
                // looking for nearest null crossing
                .filter(data -> latestValue > 0 ? data.getMacdBarValue() < 0 : data.getMacdBarValue() > 0 )
                .findFirst()
                .map(data -> {
                    var latestHill = macdData.subList(0, macdData.indexOf(data));
                    var latestExtremumData = getExtremum(latestHill);

                    if (latestHill.indexOf(latestExtremumData) == 0) {
                        log.debug("Skipping divergence processing, hill is not completed, {}", data.getTicker());
                        return NO_SIGN;
                    }

                    int startOfHill;
                    int endOfHill;
                    try {
                        startOfHill = getStartOfHill(macdData.subList(macdData.indexOf(data) + 1, macdData.size()), latestValue) + macdData.indexOf(data);
                        endOfHill = getEndOfHill(macdData.subList(startOfHill + 1, macdData.size())) + startOfHill;
                    } catch (MacdProcessingException e) {
                        return NO_SIGN;
                    }

                    var prevHill = macdData.subList(startOfHill + 1, endOfHill + 1);
                    var prevExtremumData = getExtremum(prevHill);

                    //исключение боковиков
                    var isNotClear = latestHill.stream().map(MacdData::getMacdBarValue).allMatch(bar -> abs(bar) < 0.6)
                            && prevHill.stream().map(MacdData::getMacdBarValue).allMatch(bar -> abs(bar) < 0.6);
                    if (isNotClear) {
                        return NO_SIGN;
                    }

                    var divergence = ((latestExtremumData == null || prevExtremumData == null) ? NO_SIGN
                            : checkAscDivergence(latestExtremumData, prevExtremumData) ? ASCENDING
                            : checkDescDivergence(latestExtremumData, prevExtremumData) ? DESCENDING
                            : NO_SIGN);
                    log.debug("Previous hill: {}", prevExtremumData != null ? prevExtremumData.toString() : null);
                    log.debug("Latest hill: {}", prevExtremumData != null ? prevExtremumData.toString() : null);

                    if (divergence != NO_SIGN && checkAscDivergence(latestExtremumData, prevExtremumData)
                            && checkDescDivergence(latestExtremumData, prevExtremumData)) {
                        log.warn("Received ASC and DESC divergence for {}", latestExtremumData.getTicker());
                        divergence = NO_SIGN;
                    }
                    return divergence;
                })
                .orElseGet(() -> {
                    log.info("Failed to detect hills for divergence, ticker {}", macdData.stream()
                            .findFirst()
                            .map(MacdData::getTicker)
                            .orElse("Unknown")
                    );
                    return NO_SIGN;
                });
    }

    private boolean checkAscDivergence(MacdData latest, MacdData prev) {
        return prev.getMacdBarValue() < latest.getMacdBarValue()
                && prev.getClosePrice() > latest.getClosePrice()
                && prev.getClosePrice() / latest.getClosePrice() > 1.03;
    }

    private boolean checkDescDivergence(MacdData latest, MacdData prev) {
        return abs(prev.getMacdBarValue()) > abs(latest.getMacdBarValue())
                && prev.getClosePrice() < latest.getClosePrice()
                && (latest.getClosePrice() / prev.getClosePrice()) > 1.03;
    }

    private MacdData getExtremum(List<MacdData> prevHill) {
        var extremumValue = prevHill.stream()
                .map(MacdData::getMacdBarValue)
                .mapToDouble(Double::doubleValue)
                .map(Math::abs)
                .max()
                .orElseGet(() -> {
                    log.debug("Failed to find extremum value");
                    return 0.0;
                });

        return prevHill.stream()
                .filter(macdData -> abs(macdData.getMacdBarValue()) == extremumValue)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("Failed to find extremum for latest hill");
                    return null;
                });
    }

    private int getEndOfHill(List<MacdData> list) {
        try {
            double firstBar = Optional.ofNullable(list.get(0))
                    .map(MacdData::getMacdBarValue)
                    .orElse(Double.NaN);
            return list.stream()
                    .filter(macdData -> !Double.isNaN(firstBar))
                    .filter(macdData -> firstBar > 0.0 ? macdData.getMacdBarValue() < 0.0 : macdData.getMacdBarValue() > 0.0)
                    .findFirst()
                    .map(list::indexOf)
                    .orElseThrow(() -> {
                        throw new MacdProcessingException("Failed to detect end of hill for MACD hist");
                    });
        } catch (IndexOutOfBoundsException e) {
            throw new MacdProcessingException("Failed to detect end of hill for MACD hist");
        }

    }

    private int getStartOfHill(List<MacdData> list, double latestBar) {
        try {
            double firstBar = Optional.ofNullable(list.get(0))
                    .map(MacdData::getMacdBarValue)
                    .orElse(Double.NaN);
            return list.stream()
                    .filter(macdData -> !Double.isNaN(firstBar))
                    .filter(macdData -> latestBar > 0 ? macdData.getMacdBarValue() > 0.0 : macdData.getMacdBarValue() < 0.0)
                    .filter(macdData -> firstBar > 0.0 ? macdData.getMacdBarValue() < 0.0 : macdData.getMacdBarValue() > 0.0)
                    .findFirst()
                    .map(list::indexOf)
                    .orElseThrow(() -> {
                        throw new MacdProcessingException("Failed to detect start of hill for MACD hist");
                    });
        } catch (IndexOutOfBoundsException e) {
            throw new MacdProcessingException("Failed to detect start of hill for MACD hist");
        }

    }
}
