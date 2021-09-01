package com.home.project.stocks.repository;

import com.home.project.stocks.model.repositories.CandleIndex;
import com.home.project.stocks.model.repositories.DodgeIndex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.Period;
import java.util.Date;

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
        candle.setDate(Date.from(Instant.from(Instant.now())));
        var shouldBeFoundCandle = candleRepository.save(CandleIndex.populateFields(candle, "ticker"));
        shouldBeFoundCandle = candleRepository.findById(shouldBeFoundCandle.getId())
                .orElseThrow(() -> new AssertionError("candle is not found"));

        var shouldBeFoundDodge = dodgeRepository.save(DodgeIndex.builder()
                .candleId(shouldBeFoundCandle.getId())
                .date(Date.from(Instant.now()))
                .figi("figi")
                .ticker("ticker1")
                .build());

        //should not be found
        candle = generateCandle(1, 2, 3, 4, 5);
        candle.setDate(Date.from(Instant.now()));
        var shouldNotBeFoundCandle = candleRepository.save(CandleIndex.populateFields(candle, "ticker"));
        shouldNotBeFoundCandle = candleRepository.findById(shouldNotBeFoundCandle.getId())
                .orElseThrow(() -> new AssertionError("candle is not found"));
        dodgeRepository.save(DodgeIndex.builder()
                .candleId(shouldNotBeFoundCandle.getId())
                .date(Date.from(Instant.now().minus(Period.ofDays(5))))
                .figi("figi")
                .ticker("ticker2")
                .build());

        //when
        var result = dodgeRepository.getStocksByTickerAndDateBetween(shouldBeFoundDodge.getTicker(),
                Date.from(Instant.now().minus(Period.ofDays(1))),
                Date.from(Instant.now().plus(Period.ofDays(1))));

        //then
        assertEquals(shouldBeFoundDodge.getId(), result.getId());
        assertEquals(shouldBeFoundDodge.getFigi(), result.getFigi());
        assertEquals(shouldBeFoundDodge.getDate(), result.getDate());
        assertEquals(shouldBeFoundDodge.getCandleId(), result.getCandleId());
        assertEquals(shouldBeFoundDodge.getTicker(), result.getTicker());
    }

    @Test
    @DisplayName("test save")
    public void testSave() {

        var dodgeToSave = DodgeIndex.builder()
                .candleId("sdfaljva")
                .date(Date.from(Instant.now()))
                .figi("figu")
                .ticker("ticker3")
                .build();
        var result = dodgeRepository.save(dodgeToSave);
        result = dodgeRepository.findById(result.getId())
                .orElseThrow(() -> new AssertionError("dodge not found"));

        assertEquals(dodgeToSave.getId(), result.getId());
        assertEquals(dodgeToSave.getFigi(), result.getFigi());
        assertEquals(dodgeToSave.getDate(), result.getDate());
        assertEquals(dodgeToSave.getCandleId(), result.getCandleId());
        assertEquals(dodgeToSave.getTicker(), result.getTicker());
    }

}
