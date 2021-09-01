package com.home.project.stocks.service;

import com.home.project.stocks.model.aplha.vantage.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.repositories.CandleIndex;
import com.home.project.stocks.model.repositories.DodgeIndex;
import com.home.project.stocks.model.repositories.HammerIndex;
import com.home.project.stocks.model.repositories.IndicatorsIndex;
import com.home.project.stocks.repository.CandleRepository;
import com.home.project.stocks.repository.DodgeRepository;
import com.home.project.stocks.repository.HammerRepository;
import com.home.project.stocks.repository.IndicatorRepository;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Class to populate indexes with processing results
 */
@Component
public class RepositoryService {

    private final CandleRepository candleRepository;
    private final DodgeRepository dodgeRepository;
    private final HammerRepository hammerRepository;
    private final IndicatorRepository indicatorRepository;

    public RepositoryService(CandleRepository candleRepository,
                             DodgeRepository dodgeRepository,
                             HammerRepository hammerRepository,
                             IndicatorRepository indicatorRepository) {
        this.candleRepository = candleRepository;
        this.dodgeRepository = dodgeRepository;
        this.hammerRepository = hammerRepository;
        this.indicatorRepository = indicatorRepository;
    }

    public void populateIndexes(ProcessingResult processingResult, Candle candle) {
        Objects.requireNonNull(processingResult);
        Objects.requireNonNull(candle);
        var savedCandle = candleRepository.save(CandleIndex.populateFields(candle,
                processingResult.getTicker()));
        populatePatternIndexes(processingResult, savedCandle);
        populateIndicatorIndex(processingResult, savedCandle);
    }

    private void populateIndicatorIndex(ProcessingResult processingResult, CandleIndex savedCandle) {
        indicatorRepository
                .save(IndicatorsIndex.populateFields(processingResult, savedCandle.getId(), savedCandle.getTime()));
    }

    private void populatePatternIndexes(ProcessingResult processingResult, CandleIndex savedCandle) {
        var currentDate = Date.from(Instant.now());
        if (processingResult.getIsDodge()) {
            dodgeRepository.save(DodgeIndex.builder()
                    .figi(processingResult.getFigi())
                    .ticker(processingResult.getTicker())
                    .candleId(savedCandle.getId())
                    .date(currentDate)
                    .build());
        }
        if (processingResult.getIsHammer()) {
            hammerRepository.save(HammerIndex.builder()
                    .candleId(savedCandle.getId())
                    .figi(processingResult.getFigi())
                    .ticker(processingResult.getTicker())
                    .date(currentDate)
                    .build());
        }
    }
}
