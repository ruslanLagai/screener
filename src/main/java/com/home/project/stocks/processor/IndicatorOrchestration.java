package com.home.project.stocks.processor;

import com.home.project.stocks.exceptions.ProcessingException;
import com.home.project.stocks.model.aplha.vantage.*;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.service.AlphaVantageService;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Class to collect indicator's info about stock
 */
@Component
@Log4j2
public class IndicatorOrchestration implements Orchestration {

    private final List<IndicatorProcessor> processors;
    private final AlphaVantageService alphaVantageService;
    private final Map<Class<? extends IndicatorProcessor>,
            BiFunction<AlphaVantageService, String, ParsedIndicator>> indicatorMap = new HashMap<>();

    public IndicatorOrchestration(List<IndicatorProcessor> processors,
                                  AlphaVantageService alphaVantageService) {
        this.processors = processors;
        this.alphaVantageService = alphaVantageService;

        indicatorMap.put(Ema1000Processor.class, ((indicator, ticker) ->
                indicator.getEma(ticker, Interval.ONE_DAY, EmaPeriod.ONE_THOUSAND, SeriesType.CLOSE)));
        indicatorMap.put(Ema200Processor.class, ((indicator, ticker) ->
                indicator.getEma(ticker, Interval.ONE_DAY, EmaPeriod.TWO_HUNDRED, SeriesType.CLOSE)));
        indicatorMap.put(RsiProcessor.class, ((indicator, ticker) ->
                indicator.getRsi(ticker, Interval.ONE_DAY, RsiPeriod.NINE, SeriesType.CLOSE)));
        indicatorMap.put(MacdProcessor.class, ((indicator, ticker) ->
                indicator.getMacd(ticker, Interval.ONE_DAY, SeriesType.CLOSE)));
    }

    public void processStocks(@NonNull String ticker, String figi, Map<Date, Candle> candles,
                              Date lastDate, ProcessingResult processingResult) {
        if (!StringUtils.hasText(ticker)) {
            throw new ProcessingException("Received empty ticker");
        }
        initCandleData(candles.get(lastDate), processingResult);
        processors.forEach(processor -> {
            var parsedIndicator = indicatorMap
                    .get(processor.getClass())
                    .apply(alphaVantageService, ticker);
            processor.processIndicator(parsedIndicator, candles.get(lastDate), processingResult);
        });
    }

    private void initCandleData(Candle candle, ProcessingResult processingResult) {
        processingResult.setMinPrice(candle.getLow());
        processingResult.setMaxPrice(candle.getHigh());
        processingResult.setOpenPrice(candle.getOpen());
        processingResult.setClosePrice(candle.getClose());
        processingResult.setVolume(candle.getVolume());
    }

}
