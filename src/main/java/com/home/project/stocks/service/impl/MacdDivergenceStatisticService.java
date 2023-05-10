package com.home.project.stocks.service.impl;

import com.home.project.stocks.mapper.MacdDataMapper;
import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.MacdData;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.service.CandlesService;
import com.home.project.stocks.service.IndicatorService;
import com.home.project.stocks.service.IndicatorStatisticService;
import com.home.project.stocks.utils.MacdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.home.project.stocks.utils.CandleUtils.getCandlesForPeriod;

/**
 * Macd divergence statistics processor
 *
 * 1. get last 500 daily candles
 * 2. get last 500 daily macd
 * 3. detect hills
 * 4. process negative hill if the stack has asc divergence, otherwise positive
 * 5. set win percentage
 *
 * @author rlagay
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class MacdDivergenceStatisticService implements IndicatorStatisticService {

    private static final String POSITIVE = "POSITIVE";
    private static final String NEGATIVE = "NEGATIVE";

    private final CandlesService candlesService;
    private final IndicatorService indicatorService;
    private final MacdDataMapper macdDataMapper;

    private final Map<String, BiFunction<List<Candle>, MacdData, Integer>> divergenceResultProcessors = Map.of(
        NEGATIVE, this::processAsc,
        POSITIVE, this::processDesc
    );

    private final Map<String, BiFunction<MacdData, MacdData, Boolean>> divergenceProcessors = Map.of(
        NEGATIVE, MacdUtils::checkAscDivergence,
        POSITIVE, MacdUtils::checkDescDivergence
    );

    @Value("${indicator.macd.statistics.divergence.candles}")
    private int days;

    @Override
    public void analyzeStock(ProcessingResult processingResult, Interval interval) {
        var ticker = processingResult.getTicker();
        if (processingResult.getMacdDivergence() == null
            || processingResult.getMacdDivergence() == ProcessingResult.Trend.NO_SIGN) {
            return;
        }

        log.debug("Analyzing macd divergence statistic for {}", ticker);

        var candles = candlesService.getHistoricalCandles(ticker, interval, 500);
        var indicator = indicatorService.getHistoricalMacd(ticker, interval, 500);
        var macdData = macdDataMapper.toMacdData(indicator, candles);

        int goodSignalNumber = 0;
        int totalSignalNumber = 0;
        var key = processingResult.getMacdDivergence().equals(ProcessingResult.Trend.ASCENDING)
            ? NEGATIVE : POSITIVE;
        log.debug("Processing {} divergence", key);

        var hills = getMacdHills(macdData).get(key);
        for (MacdData prevExtremum : hills) {
            if (hills.indexOf(prevExtremum) + 1 >= hills.size()) {
                break;
            }
            var nextExtremum = hills.get(hills.indexOf(prevExtremum) + 1);
            boolean isDivergence = divergenceProcessors.get(key).apply(nextExtremum, prevExtremum);
            if (isDivergence) {
                var candlesAfterDivergence = getCandlesForPeriod(candles, nextExtremum.getDateTime(),
                    nextExtremum.getDateTime().plusDays(days));
                if (candlesAfterDivergence.isEmpty()) {
                    log.warn("Failed to fetch candles from DB for divergence analytic, ticker {}", ticker);
                    continue;
                }
                int procResult = divergenceResultProcessors.get(key).apply(candlesAfterDivergence, nextExtremum);
                goodSignalNumber = goodSignalNumber + procResult;
                totalSignalNumber++;
            }
        }
        double winPercentage = Integer.valueOf(goodSignalNumber).doubleValue() / Integer.valueOf(totalSignalNumber).doubleValue();
        processingResult.setMacdDivergenceStatistics(winPercentage);
    }

    /**
     * Check candles after macd extremum
     * 0 - if difference < 2%
     * 1 - if difference > 2%
     * @param candles - list of cadnles to be checked
     * @param macdData - macd extremum
     * @return - result
     */
    private int processAsc(List<Candle> candles, MacdData macdData) {
        var candleMaxPrice = candles.stream().max(Comparator.comparing(Candle::getC))
            .orElse(Candle.builder().build());
        double priceDifference = candleMaxPrice.getC() / macdData.getClosePrice();
        return priceDifference > 1.02 ? 1 : 0;
    }

    /**
     * Check candles after macd extremum
     * 0 - if difference < 2%
     * 1 - if difference > 2%
     * @param candles - list of cadnles to be checked
     * @param macdData - macd extremum
     * @return - result
     */
    private int processDesc(List<Candle> candles, MacdData macdData) {
        var candleMinPrice = candles.stream().min(Comparator.comparing(Candle::getC))
            .orElse(Candle.builder().build());
        double priceDifference = macdData.getClosePrice() / candleMinPrice.getC();
        return priceDifference > 1.02 ? 1 : 0;
    }


    private Map<String, List<MacdData>> getMacdHills(List<MacdData> macdData) {
        if (CollectionUtils.isEmpty(macdData)) {
            log.warn("Empty macd data");
            return Map.of();
        }
        macdData = macdData.stream().sorted(Comparator.comparing(MacdData::getDateTime)).toList();

        List<MacdData> hill = new ArrayList<>();
        List<MacdData> positiveHill = new ArrayList<>();
        List<MacdData> negativeHill = new ArrayList<>();


        int sign = macdData.get(0).getMacdBarValue() > 0 ? 1 : -1;
        for (MacdData macd : macdData) {
            boolean isSameHill = (sign == 1 && macd.getMacdBarValue() > 0)
                || (sign == -1 && macd.getMacdBarValue() < 0);
            if (isSameHill) {
                hill.add(macd);
                continue;
            }
            var extremum = MacdUtils.getExtremum(hill);
            if (sign == 1) {
                positiveHill.add(extremum);
            } else {
                negativeHill.add(extremum);
            }
            hill = new ArrayList<>();
            sign = macd.getMacdBarValue() > 0 ? 1 : -1;
        }

        return Map.of(
            POSITIVE, positiveHill,
            NEGATIVE, negativeHill
        );
    }
}
