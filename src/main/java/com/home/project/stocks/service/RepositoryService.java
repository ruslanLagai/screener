package com.home.project.stocks.service;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.repositories.CandleIndex;
import com.home.project.stocks.model.repositories.DodgeIndex;
import com.home.project.stocks.model.repositories.HammerIndex;
import com.home.project.stocks.processor.StocksProcessor;
import com.home.project.stocks.repository.CandleRepository;
import com.home.project.stocks.repository.DodgeRepository;
import com.home.project.stocks.repository.HammerRepository;
import com.home.project.stocks.utils.DurationParser;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.ElasticsearchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Log4j2
public class RepositoryService {

    private DodgeRepository dodgeRepository;
    private HammerRepository hammerRepository;
    private CandleRepository candleRepository;
    private final Function<Candle, HammerIndex> hammerIndexFunction = this::findHammerStocks;
    private final Function<Candle, DodgeIndex> dodgesIndexFunction = this::findDodgeStocks;

    @Autowired
    public void setHammerRepository(HammerRepository hammerRepository) {
        this.hammerRepository = hammerRepository;
    }

    @Autowired
    public void setCandleRepository(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    @Autowired
    public void setDodgeRepository(DodgeRepository dodgeRepository) {
        this.dodgeRepository = dodgeRepository;
    }

    public Set<ProcessingResult> save(Set<ProcessingResult> results) {
        filterAlreadySent(results);
        saveHammerPatterns(results);
        saveDodgePatterns(results);
        return results;
    }

    private void filterAlreadySent(Collection<ProcessingResult> results) {
        results.forEach(item -> {
            var multiValueMap = item.getProcessedCandles();
            multiValueMap.replace(StocksProcessor.Processors.DODGE,
                    filterAlreadySentStocks(multiValueMap.get(StocksProcessor.Processors.DODGE), dodgesIndexFunction));
            multiValueMap.replace(StocksProcessor.Processors.HAMMER,
                    filterAlreadySentStocks(multiValueMap.get(StocksProcessor.Processors.HAMMER), hammerIndexFunction));
        });
    }

    private List<Candle> filterAlreadySentStocks(List<Candle> candles, Function function) {
        return candles.stream()
                .filter(candle -> function.apply(candle) == null)
                .collect(Collectors.toList());
    }

    private DodgeIndex findDodgeStocks(Candle candle) {
        return dodgeRepository.getStocksByFigiAndCandleDateBetween(candle.getFigi(),
                                candle.getTime().minus(DurationParser.parseInterval(candle.getInterval())),
                                candle.getTime().plus(DurationParser.parseInterval(candle.getInterval())));
    }

    private HammerIndex findHammerStocks(Candle candle) {
        return hammerRepository.getStocksByFigiAndCandleDateBetween(candle.getFigi(),
                candle.getTime().minus(DurationParser.parseInterval(candle.getInterval())),
                candle.getTime().plus(DurationParser.parseInterval(candle.getInterval())));
    }

    private void saveDodgePatterns(Collection<ProcessingResult> results) {
        try {
            results.stream().filter(ProcessingResult::getIsDodge)
                    .forEach(item -> item.getProcessedCandles().get(StocksProcessor.Processors.DODGE).stream()
                            .filter(Objects::nonNull)
                            .map(candle -> {
                                var candleIndex = candleRepository.save(CandleIndex.populateFields(candle));
                                return DodgeIndex.builder()
                                        .figi(candle.getFigi())
                                        .ticker(item.getTicker())
                                        .candleDate(candle.getTime())
                                        .date(LocalDateTime.now(ZoneId.systemDefault()))
                                        .candleId(candleIndex.getId())
                                        .build();
                            })
                            .forEach(dodgeIndex -> dodgeRepository.save(dodgeIndex)));
        } catch (ElasticsearchException e) {
            log.warn("Failed to save dodge pattern. Figi: " + results.iterator().next().getFigi());
            throw e;
        }
    }

    private void saveHammerPatterns(Collection<ProcessingResult> results) {
        try {
            results.stream().filter(ProcessingResult::getIsHammer)
                    .forEach(item -> item.getProcessedCandles().get(StocksProcessor.Processors.HAMMER).stream()
                            .filter(Objects::nonNull)
                            .map(candle -> {
                                var candleIndex = candleRepository.save(CandleIndex.populateFields(candle));
                                return HammerIndex.builder()
                                        .figi(candle.getFigi())
                                        .ticker(item.getTicker())
                                        .candleDate(candle.getTime())
                                        .date(LocalDateTime.now(ZoneId.systemDefault()))
                                        .candleId(candleIndex.getId())
                                        .build();
                            })
                            .forEach(hammerIndex -> hammerRepository.save(hammerIndex)));
        } catch (ElasticsearchException e) {
            log.warn("Failed to save hammer pattern. Figi: " + results.iterator().next().getFigi());
            throw e;
        }
    }
}
