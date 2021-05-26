package com.home.project.stocks.service;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.repositories.CandleIndex;
import com.home.project.stocks.model.repositories.DodgeIndex;
import com.home.project.stocks.model.repositories.HammerIndex;
import com.home.project.stocks.processor.AbstractProcessorTest;
import com.home.project.stocks.processor.StocksProcessor;
import com.home.project.stocks.repository.CandleRepository;
import com.home.project.stocks.repository.DodgeRepository;
import com.home.project.stocks.repository.HammerRepository;
import org.apache.commons.compress.utils.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * class to test {@link com.home.project.stocks.service.RepositoryService}
 */
@DisplayName("Test repository service with mocks")
@ExtendWith(MockitoExtension.class)
class RepositoryServiceTest extends AbstractProcessorTest {

    private static final String FIGI = "figi";
    private static final String TICKER = "ticker";
    private static final String ID_1 = "asdfrwq12";
    private static final String ID_2 = "asdfrwq13";

    RepositoryService repositoryService = new RepositoryService();

    HammerRepository hammerRepository;
    DodgeRepository dodgeRepository;
    CandleRepository candleRepository;

    @BeforeEach
    void setUp() {
        hammerRepository = mock(HammerRepository.class);
        dodgeRepository = mock(DodgeRepository.class);
        candleRepository = mock(CandleRepository.class);

        repositoryService.setHammerRepository(hammerRepository);
        repositoryService.setCandleRepository(candleRepository);
        repositoryService.setDodgeRepository(dodgeRepository);
    }

    @Test
    @DisplayName("test dodge - no already sent")
    void testSaveDodgeNoAlreadySent() {
        //given
        var dodgeCandle = generateCandle(20.1, 20.5, 28, 14, 5);
        dodgeCandle.setTime(LocalDateTime.now());
        dodgeCandle.setInterval("day");
        var dodgeCandle1 = generateCandle(20.1, 20.5, 28, 14, 5);
        dodgeCandle1.setTime(LocalDateTime.now());
        dodgeCandle1.setInterval("day");

        var hammerCandle = generateCandle(10, 18, 21, 8, 5);
        hammerCandle.setTime(LocalDateTime.now());
        hammerCandle.setInterval("day");

        var processingResult = mockProcessingResult(true, false);
        MultiValueMap<StocksProcessor.Processors, Candle> candles = new LinkedMultiValueMap<>();
        candles.add(StocksProcessor.Processors.DODGE, dodgeCandle);
        candles.add(StocksProcessor.Processors.DODGE, dodgeCandle1);
        candles.add(StocksProcessor.Processors.HAMMER, hammerCandle);
        processingResult.setProcessedCandles(candles);

        //when
        var candleIndex = CandleIndex.populateFields(dodgeCandle);
        candleIndex.setId(ID_1);
        var candleIndex1 = CandleIndex.populateFields(dodgeCandle1);
        candleIndex1.setId(ID_2);
        doReturn(candleIndex1).when(candleRepository).save(CandleIndex.populateFields(dodgeCandle1));

        var result = repositoryService.save(Sets.newHashSet(processingResult));

        //then
        verify(hammerRepository, times(0)).save(any());
        verify(dodgeRepository, times(2)).save(any());

        assertEquals(1, result.size());
        result.forEach(item -> assertAll(() -> {
            assertEquals(2, item.getProcessedCandles().get(StocksProcessor.Processors.DODGE).size());
            assertEquals(1, item.getProcessedCandles().get(StocksProcessor.Processors.HAMMER).size());
            assertTrue(item.getIsDodge());
        }));
    }

    @Test
    @DisplayName("test hammer - no already sent")
    void testSaveHammerNoAlreadySent() {
        //given
        var dodgeCandle = generateCandle(20.1, 20.5, 28, 14, 5);
        dodgeCandle.setTime(LocalDateTime.now());
        dodgeCandle.setInterval("day");

        var hammerCandle = generateCandle(10, 18, 21, 8, 5);
        hammerCandle.setTime(LocalDateTime.now());
        hammerCandle.setInterval("day");

        var hammerCandle1 = generateCandle(10, 18, 21, 8, 5);
        hammerCandle1.setTime(LocalDateTime.now());
        hammerCandle1.setInterval("day");

        var processingResult = mockProcessingResult(false, true);
        MultiValueMap<StocksProcessor.Processors, Candle> candles = new LinkedMultiValueMap<>();
        candles.add(StocksProcessor.Processors.DODGE, dodgeCandle);
        candles.add(StocksProcessor.Processors.HAMMER, hammerCandle1);
        candles.add(StocksProcessor.Processors.HAMMER, hammerCandle);
        processingResult.setProcessedCandles(candles);

        //when
        var candleIndex = CandleIndex.populateFields(hammerCandle);
        candleIndex.setId(ID_1);
        doReturn(candleIndex).when(candleRepository).save(CandleIndex.populateFields(hammerCandle));
        var candleIndex1 = CandleIndex.populateFields(hammerCandle1);
        candleIndex1.setId(ID_2);

        var result = repositoryService.save(Sets.newHashSet(processingResult));

        //then
        verify(dodgeRepository, times(0)).save(any());
        verify(hammerRepository, times(2)).save(any());

        assertEquals(1, result.size());
        result.forEach(item -> assertAll(() -> {
            assertEquals(1, item.getProcessedCandles().get(StocksProcessor.Processors.DODGE).size());
            assertEquals(2, item.getProcessedCandles().get(StocksProcessor.Processors.HAMMER).size());
            assertTrue(item.getIsHammer());
        }));
    }

    @Test
    @DisplayName("test hammer & dodge - no sent")
    void testSaveHammerDodgeNoAlreadySent() {
        //given
        var dodgeCandle = generateCandle(20.1, 20.5, 28, 14, 5);
        dodgeCandle.setTime(LocalDateTime.now());
        dodgeCandle.setInterval("day");

        var hammerCandle = generateCandle(10, 18, 21, 8, 5);
        hammerCandle.setTime(LocalDateTime.now());
        hammerCandle.setInterval("day");

        var processingResult = mockProcessingResult(true, true);
        MultiValueMap<StocksProcessor.Processors, Candle> candles = new LinkedMultiValueMap<>();
        candles.add(StocksProcessor.Processors.DODGE, dodgeCandle);
        candles.add(StocksProcessor.Processors.HAMMER, hammerCandle);
        processingResult.setProcessedCandles(candles);

        //when
        var candleIndex = CandleIndex.populateFields(hammerCandle);
        candleIndex.setId(ID_1);
        doReturn(candleIndex).when(candleRepository).save(CandleIndex.populateFields(hammerCandle));
        var candleIndex1 = CandleIndex.populateFields(dodgeCandle);
        candleIndex1.setId(ID_2);
        doReturn(candleIndex1).when(candleRepository).save(CandleIndex.populateFields(dodgeCandle));

        var result = repositoryService.save(Sets.newHashSet(processingResult));

        //then
        verify(dodgeRepository, times(1)).save(any());
        verify(hammerRepository, times(1)).save(any());

        assertEquals(1, result.size());
        result.forEach(item -> assertAll(() -> {
            assertEquals(1, item.getProcessedCandles().get(StocksProcessor.Processors.DODGE).size());
            assertEquals(1, item.getProcessedCandles().get(StocksProcessor.Processors.HAMMER).size());
            assertTrue(item.getIsHammer());
            assertTrue(item.getIsDodge());
        }));
    }

    @Test
    @DisplayName("test hammer & dodge - already sent")
    void testSaveHammerDodge() {
        //given
        var dodgeCandle = generateCandle(20.1, 20.5, 28, 14, 5);
        dodgeCandle.setTime(LocalDateTime.now());
        dodgeCandle.setInterval("day");

        var hammerCandle = generateCandle(10, 18, 21, 8, 5);
        hammerCandle.setTime(LocalDateTime.now());
        hammerCandle.setInterval("day");

        var processingResult = mockProcessingResult(true, true);
        MultiValueMap<StocksProcessor.Processors, Candle> candles = new LinkedMultiValueMap<>();
        candles.add(StocksProcessor.Processors.DODGE, dodgeCandle);
        candles.add(StocksProcessor.Processors.HAMMER, hammerCandle);
        processingResult.setProcessedCandles(candles);

        //when
        doReturn(DodgeIndex.builder().figi(FIGI).build()).when(dodgeRepository)
                .getStocksByFigiAndCandleDateBetween(any(), any(), any());
        doReturn(HammerIndex.builder().figi(FIGI).build()).when(hammerRepository)
                .getStocksByFigiAndCandleDateBetween(any(), any(), any());

        var result = repositoryService.save(Sets.newHashSet(processingResult));

        //then
        verify(dodgeRepository, times(0)).save(any());
        verify(hammerRepository, times(0)).save(any());

        assertEquals(0, result.iterator().next().getProcessedCandles().get(StocksProcessor.Processors.HAMMER).size());
        assertEquals(0, result.iterator().next().getProcessedCandles().get(StocksProcessor.Processors.DODGE).size());

    }

    private ProcessingResult mockProcessingResult(boolean isDodge, boolean isHammer) {
        var processingResult = new ProcessingResult();
        processingResult.setFigi(FIGI);
        processingResult.setTicker(TICKER);
        processingResult.setIsDodge(isDodge);
        processingResult.setIsHammer(isHammer);
        return processingResult;
    }

}