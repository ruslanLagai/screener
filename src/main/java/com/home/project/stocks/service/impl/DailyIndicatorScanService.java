package com.home.project.stocks.service.impl;

import com.home.project.stocks.model.api.EmaPeriod;
import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.model.api.SeriesType;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.processor.Ema200Processor;
import com.home.project.stocks.processor.IndicatorProcessor;
import com.home.project.stocks.service.CandlesService;
import com.home.project.stocks.service.DailyScanService;
import com.home.project.stocks.service.DbUpdateService;
import com.home.project.stocks.service.IndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Service to process ema
 *  200 on weekly TF
 *  10/20 on daily TF - Kell
 *
 * @author rlagay
 */
@Service
@Slf4j
public class DailyIndicatorScanService implements DailyScanService {

    private final IndicatorService dailyIndicatorService;
    private final CandlesService candlesService;
    private final List<IndicatorProcessor> indicatorProcessors;
    private final DbUpdateService dbUpdateService;

    private final Map<Class<? extends IndicatorProcessor>, BiFunction<IndicatorService, String, ParsedIndicator>>
            indicatorMap = Map.of(
                    Ema200Processor.class, ((indicatorService, ticker) ->
                    indicatorService.getEma(ticker, Interval.TWELVE_DATA_ONE_WEEK, EmaPeriod.TWO_HUNDRED, SeriesType.CLOSE))
    );

    public DailyIndicatorScanService(IndicatorService dailyIndicatorService,
                                     CandlesService candlesService,
                                     List<IndicatorProcessor> indicatorProcessors,
                                     DbUpdateService dbUpdateService) {
        this.dailyIndicatorService = dailyIndicatorService;
        this.candlesService = candlesService;
        this.indicatorProcessors = indicatorProcessors;
        this.dbUpdateService = dbUpdateService;
    }

    @Override
    public void processStock(String ticker, String figi) {
        var processingResult = new ProcessingResult();
        var candle = candlesService.getCandles(ticker, Interval.TWELVE_DATA_ONE_DAY).stream()
                .max(Comparator.comparing(Candle::getDatetime))
                .orElse(null);
        if (candle == null) {
            log.warn("Received null candle, ticker {}", ticker);
            return;
        }
        initCandleData(candle, processingResult, ticker, figi);
        indicatorProcessors.forEach(processor -> {
            var function = indicatorMap.get(processor.getClass());
            if (function != null) {
                var indicator = function.apply(dailyIndicatorService, ticker);
                processor.processIndicator(indicator, candle, processingResult);
            }
        });
        dbUpdateService.saveIndicatorData(processingResult);
    }

    private void initCandleData(Candle candle, ProcessingResult processingResult, String ticker, String figi) {
        processingResult.setMinPrice(candle.getL());
        processingResult.setMaxPrice(candle.getH());
        processingResult.setOpenPrice(candle.getO());
        processingResult.setClosePrice(candle.getC());
        processingResult.setVolume(candle.getV());
        processingResult.setTicker(ticker);
        processingResult.setFigi(figi);
    }
}
