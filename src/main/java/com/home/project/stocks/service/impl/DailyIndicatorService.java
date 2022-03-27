package com.home.project.stocks.service.impl;

import com.home.project.stocks.client.TwelvedataApiClient;
import com.home.project.stocks.exceptions.IndicatorParsingException;
import com.home.project.stocks.model.api.EmaPeriod;
import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.model.api.RsiPeriod;
import com.home.project.stocks.model.api.SeriesType;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.model.entity.DailyIndicator;
import com.home.project.stocks.parser.TwelveDataParser;
import com.home.project.stocks.repository.DailyIndicatorDataRepository;
import com.home.project.stocks.service.DbUpdateService;
import com.home.project.stocks.service.IndicatorService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

/**
 * @author rlagay
 */
@Service
@Slf4j
public class DailyIndicatorService implements IndicatorService {

    private final TwelvedataApiClient apiClient;
    private final DailyIndicatorDataRepository dailyIndicatorDataRepository;
    private final DbUpdateService dbUpdateService;

    public DailyIndicatorService(TwelvedataApiClient apiClient,
                                 DailyIndicatorDataRepository dailyIndicatorDataRepository,
                                 DbUpdateService dbUpdateService) {
        this.apiClient = apiClient;
        this.dailyIndicatorDataRepository = dailyIndicatorDataRepository;
        this.dbUpdateService = dbUpdateService;
    }

    @Override
    @Transactional
    public ParsedIndicator getEma(String ticker, Interval interval, EmaPeriod emaPeriod, SeriesType seriesType) {
        DailyIndicator parsedIndicator;
        var saved = Optional.ofNullable(dailyIndicatorDataRepository
                            .getByTickerAndDateAfter(ticker, LocalDate.now().atTime(LocalTime.MIN)));
        if (saved.isPresent() && saved.get().getEmaData().stream()
                .anyMatch(emaIndex -> emaIndex.getEmaType().equals(emaPeriod.getPeriod()))) {
            parsedIndicator = saved.get();
        } else {
            try {
                var newEma = apiClient.getEma(ticker, interval.getInterval(), emaPeriod.getPeriod());
                parsedIndicator = TwelveDataParser.parseEma(newEma);
                dbUpdateService.updateEmaOnDailyIndicator(parsedIndicator);
            } catch (FeignException e) {
                log.error("Failed to retrieve ema from twelve data", e);
                parsedIndicator = DailyIndicator.builder().build();
            } catch (IndicatorParsingException e) {
                log.error("Failed to parse indicator - no data", e);
                parsedIndicator = DailyIndicator.builder().build();
            }
        }

        return TwelveDataParser.convertToParsedIndicator(parsedIndicator);
    }

    @Override
    @Transactional
    public ParsedIndicator getRsi(String ticker, Interval interval, RsiPeriod rsiPeriod, SeriesType seriesType) {
        DailyIndicator parsedIndicator;
        var saved = Optional.ofNullable(dailyIndicatorDataRepository
                .getByTickerAndDateAfter(ticker, LocalDate.now().minusDays(1).atTime(LocalTime.MIN)));
        if (saved.isPresent() && !CollectionUtils.isEmpty(saved.get().getRsiData())) {
            parsedIndicator = saved.get();
        } else {
            try {
                var rsi = apiClient.getRsi(ticker, interval.getInterval(), rsiPeriod.getPeriod());
                parsedIndicator = TwelveDataParser.parseRsi(rsi);
                dbUpdateService.updateRsiOnDailyIndicator(parsedIndicator);
            } catch (FeignException e) {
                log.error("Failed to retrieve rsi from twelve data", e);
                parsedIndicator = DailyIndicator.builder().build();
            } catch (IndicatorParsingException e) {
                log.error("Failed to parse indicator - no data", e);
                parsedIndicator = DailyIndicator.builder().build();
            }
        }
        return TwelveDataParser.convertToParsedIndicator(parsedIndicator);
    }


    @Override
    @Transactional
    public ParsedIndicator getMacd(String ticker, Interval interval, SeriesType seriesType) {
        DailyIndicator parsedIndicator;
        var saved = Optional.ofNullable(dailyIndicatorDataRepository
                .getByTickerAndDateAfter(ticker, LocalDate.now().atTime(LocalTime.MIN)));
        if (saved.isPresent() && !CollectionUtils.isEmpty(saved.get().getMacdData())) {
            parsedIndicator = saved.get();
        } else {
            try {
                var macd = apiClient.getMacd(ticker, interval.getInterval());
                parsedIndicator = TwelveDataParser.parseMacd(macd);
                dbUpdateService.updateEmaOnDailyIndicator(parsedIndicator);
            } catch (FeignException e) {
                log.error("Failed to retrieve macd from twelve data", e);
                parsedIndicator = DailyIndicator.builder().build();
            } catch (IndicatorParsingException e) {
                log.error("Failed to parse indicator - no data", e);
                parsedIndicator = DailyIndicator.builder().build();
            }
        }
        return TwelveDataParser.convertToParsedIndicator(parsedIndicator);
    }

}
