package com.home.project.stocks.repository;

import com.home.project.stocks.model.repositories.CandleIndex;
import com.home.project.stocks.model.repositories.DodgeIndex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DisplayName("Test dodge repo")
public class DodgeRepositoryTest extends AbstractRepositoryTest {

    @Test
    @DisplayName("test find pattern for period")
    public void testFindPattern() {

        //given
        //should be found
        var candle = generateCandle(1, 2, 3, 4, 5);
        candle.setFigi("figi");
        candle.setInterval("day");
        candle.setTime(LocalDateTime.of(2021, 6, 14, 1,32,44));
        var shouldBeFoundCandle = candleRepository.save(CandleIndex.populateFields(candle));
        shouldBeFoundCandle = candleRepository.findById(shouldBeFoundCandle.getId())
                .orElseThrow(() -> new AssertionError("candle is not found"));

        var shouldBeFoundDodge = dodgeRepository.save(DodgeIndex.builder()
                .candleId(shouldBeFoundCandle.getId())
                .candleDate(LocalDateTime.of(2021, 6, 14, 1,32,44))
                .date(LocalDateTime.of(2021, 7, 17, 1,32,44))
                .figi("figi")
                .ticker("ticker")
                .build());
        //should not be found
        candle = generateCandle(1, 2, 3, 4, 5);
        candle.setFigi("figi");
        candle.setInterval("day");
        candle.setTime(LocalDateTime.of(2021, 6, 16, 1,32,44));
        var shouldNotBeFoundCandle = candleRepository.save(CandleIndex.populateFields(candle));
        shouldNotBeFoundCandle = candleRepository.findById(shouldNotBeFoundCandle.getId())
                .orElseThrow(() -> new AssertionError("candle is not found"));
        dodgeRepository.save(DodgeIndex.builder()
                .candleId(shouldNotBeFoundCandle.getId())
                .candleDate(LocalDateTime.of(2021, 6, 16, 1,32,44))
                .date(LocalDateTime.of(2021, 7, 17, 1,32,44))
                .figi("figi")
                .ticker("ticker")
                .build());

        //when
        var result = dodgeRepository.getStocksByFigiAndCandleDateBetween(shouldBeFoundDodge.getFigi(),
                LocalDateTime.of(2021, 6, 14, 1,32,43),
                LocalDateTime.of(2021, 6, 15, 1,32,44));

        //then
        assertEquals(shouldBeFoundDodge.getId(), result.getId());
        assertEquals(shouldBeFoundDodge.getFigi(), result.getFigi());
        assertEquals(shouldBeFoundDodge.getCandleDate(), result.getCandleDate());
        assertEquals(shouldBeFoundDodge.getDate(), result.getDate());
        assertEquals(shouldBeFoundDodge.getCandleId(), result.getCandleId());
        assertEquals(shouldBeFoundDodge.getTicker(), result.getTicker());
    }

    @Test
    @DisplayName("test save")
    public void testSave() {

        var dodgeToSave = DodgeIndex.builder()
                .candleId("sdfaljva")
                .candleDate(LocalDateTime.of(2021, 6, 14, 1,32,44))
                .date(LocalDateTime.of(2021, 6, 14, 1,32,44))
                .figi("figu")
                .ticker("ticker")
                .build();
        var result = dodgeRepository.save(dodgeToSave);
        result = dodgeRepository.findById(result.getId())
                .orElseThrow(() -> new AssertionError("dodge not found"));

        assertEquals(dodgeToSave.getId(), result.getId());
        assertEquals(dodgeToSave.getFigi(), result.getFigi());
        assertEquals(dodgeToSave.getCandleDate(), result.getCandleDate());
        assertEquals(dodgeToSave.getDate(), result.getDate());
        assertEquals(dodgeToSave.getCandleId(), result.getCandleId());
        assertEquals(dodgeToSave.getTicker(), result.getTicker());
    }

}
