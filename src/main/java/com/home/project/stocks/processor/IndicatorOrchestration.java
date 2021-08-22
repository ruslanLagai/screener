package com.home.project.stocks.processor;

import com.home.project.stocks.exceptions.ProcessingException;
import com.home.project.stocks.model.aplha.vantage.EmaPeriod;
import com.home.project.stocks.model.aplha.vantage.Interval;
import com.home.project.stocks.model.aplha.vantage.RsiPeriod;
import com.home.project.stocks.model.aplha.vantage.SeriesType;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.service.IndicatorService;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Class to collect indicator's info about stock
 */
@Component
@Log4j2
public class IndicatorOrchestration implements Orchestration {

    private final List<IndicatorProcessor> processors;
    private final IndicatorService indicatorService;
    private final Map<Class<? extends IndicatorProcessor>,
            BiFunction<IndicatorService, String, ParsedIndicator>> indicatorMap = new HashMap<>();

    public IndicatorOrchestration(List<IndicatorProcessor> processors,
                                  IndicatorService indicatorService) {
        this.processors = processors;
        this.indicatorService = indicatorService;

        indicatorMap.put(Ema1000Processor.class, ((indicator, ticker) ->
                indicator.getEma(ticker, Interval.ONE_DAY, EmaPeriod.ONE_THOUSAND, SeriesType.CLOSE)));
        indicatorMap.put(Ema200Processor.class, ((indicator, ticker) ->
                indicator.getEma(ticker, Interval.ONE_DAY, EmaPeriod.TWO_HUNDRED, SeriesType.CLOSE)));
        indicatorMap.put(RsiProcessor.class, ((indicator, ticker) ->
                indicator.getRsi(ticker, Interval.ONE_DAY, RsiPeriod.NINE, SeriesType.CLOSE)));
        indicatorMap.put(MacdProcessor.class, ((indicator, ticker) ->
                indicator.getMacd(ticker, Interval.ONE_DAY, SeriesType.CLOSE)));
    }

    public void processStocks(@NonNull String ticker, String figi, Candle[] candles,
                                          Candle candle, ProcessingResult processingResult) {
        Objects.requireNonNull(candle);
        if (!StringUtils.hasText(ticker)) {
            throw new ProcessingException("Received empty ticker");
        }
        initCandleData(candle, processingResult);
        processors.forEach(processor -> {
            var parsedIndicator = indicatorMap
                    .get(processor.getClass())
                    .apply(indicatorService, ticker);
            processor.processIndicator(parsedIndicator, candle, processingResult);
        });
    }

    private void initCandleData(Candle candle, ProcessingResult processingResult) {
        processingResult.setMinPrice(candle.getL());
        processingResult.setMaxPrice(candle.getH());
        processingResult.setOpenPrice(candle.getO());
        processingResult.setClosePrice(candle.getC());
        processingResult.setVolume(candle.getV());
    }

}
