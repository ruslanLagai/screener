package com.home.project.stocks.repository;

import com.home.project.stocks.model.aplha.vantage.EmaPeriod;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.candles.Interval;
import com.home.project.stocks.model.processing.ProcessingResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class to test {@link RepositorySaver}
 */
@ExtendWith(SpringExtension.class)
class RepositorySaverTest extends AbstractRepositoryTest {

    @Autowired
    RepositorySaver repositorySaver;

    @Autowired
    HammerRepository hammerRepository;

    @Autowired
    IndicatorRepository indicatorRepository;

    @Test
    @DisplayName("test save")
    void populateIndexes() {
        //given
        var processingResult = new ProcessingResult();
        processingResult.setFigi("figi");
        processingResult.setTicker("ticker");
        processingResult.setIsDodge(true);
        processingResult.setIsHammer(true);
        processingResult.setVolume(10);
        processingResult.setClosePrice(11);
        processingResult.setOpenPrice(9);
        processingResult.setMaxPrice(11);
        processingResult.setMinPrice(8);
        processingResult.setMacdBarTrend(ProcessingResult.Trend.ASCENDING);
        processingResult.setMacdSignalTrend(ProcessingResult.Trend.ASCENDING);
        processingResult.setMacdBarValues(Arrays.asList(1.0, 2.0, 3.0));
        processingResult.setRsiSign(ProcessingResult.RsiSign.OVERBOUGHT);
        processingResult.setRsiValues(Arrays.asList(70.0, 67.0));
        processingResult.setEmaValue(Map.of(
                EmaPeriod.TWO_HUNDRED, ProcessingResult.EmaData.builder()
                        .emaValue(1.0)
                        .difference(2.0)
                        .levelType(ProcessingResult.LevelType.SUPPORT)
                        .isCloseToEma(true).build(),
                EmaPeriod.ONE_THOUSAND, ProcessingResult.EmaData.builder()
                        .emaValue(3.0)
                        .difference(4.0)
                        .levelType(ProcessingResult.LevelType.SUPPORT)
                        .isCloseToEma(true).build()
        ));
        var candle = new Candle();
        candle.setV(processingResult.getVolume());
        candle.setL(processingResult.getMinPrice());
        candle.setH(processingResult.getMaxPrice());
        candle.setO(processingResult.getOpenPrice());
        candle.setC(processingResult.getClosePrice());
        candle.setInterval(Interval.ONE_DAY.getPeriod());
        candle.setTime(LocalDateTime.now());

        //when
        repositorySaver.populateIndexes(processingResult, candle);

        //then
        var dodges = dodgeRepository.findAll();
        var hammers = hammerRepository.findAll();
        var indicators = indicatorRepository.findAll();

        assertAll(() -> {
            assertTrue(dodges.iterator().hasNext());
            assertTrue(hammers.iterator().hasNext());
            assertTrue(indicators.iterator().hasNext());

            assertEquals("ticker", dodges.iterator().next().getTicker());
            assertEquals("ticker", hammers.iterator().next().getTicker());
            assertEquals("ticker", indicators.iterator().next().getTicker());
            assertEquals(ProcessingResult.RsiSign.OVERBOUGHT.name(), indicators.iterator().next().getRsiSign());
            assertEquals(ProcessingResult.Trend.ASCENDING.name(), indicators.iterator().next().getMacdBarTrend());
            assertEquals(ProcessingResult.Trend.ASCENDING.name(), indicators.iterator().next().getMacdSignalTrend());
            assertEquals(2, indicators.iterator().next().getEmaData().size());
            assertEquals(ProcessingResult.LevelType.SUPPORT.name(),
                    indicators.iterator().next().getEmaData().get(0).getLevelType());
            assertTrue(indicators.iterator().next().getEmaData().get(1).isCloseToEma());
            assertEquals(candle.getTime(), indicators.iterator().next().getDate());
        });
    }

}