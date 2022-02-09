package com.home.project.stocks.repository;

import com.home.project.stocks.model.repositories.CandleIndex;
import com.home.project.stocks.model.repositories.DodgeIndex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DisplayName("Test dodge repo")
@ContextConfiguration(classes = AbstractRepositoryTest.Config.class)
public class DodgeRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    CandleRepository candleRepository;

    @Autowired
    DodgeRepository dodgeRepository;

    static {
        container.start();
    }

    @Test
    @DisplayName("test find pattern for period")
    public void testFindPattern() {

        //given
        //should be found
        var candle = generateCandle(1, 2, 3, 4, 5, LocalDateTime.now());
        var shouldBeFoundCandle = candleRepository.save(CandleIndex.populateFields(candle, "ticker"));
        shouldBeFoundCandle = candleRepository.findById(shouldBeFoundCandle.getId())
                .orElseThrow(() -> new AssertionError("candle is not found"));

        var shouldBeFoundDodge = dodgeRepository.save(DodgeIndex.builder()
                .candleId(shouldBeFoundCandle.getId())
                .date(LocalDateTime.now())
                .figi("figi")
                .ticker("ticker1")
                .build());

        //should not be found
        candle = generateCandle(1, 2, 3, 4, 5, LocalDateTime.now());
        var shouldNotBeFoundCandle = candleRepository.save(CandleIndex.populateFields(candle, "ticker"));
        shouldNotBeFoundCandle = candleRepository.findById(shouldNotBeFoundCandle.getId())
                .orElseThrow(() -> new AssertionError("candle is not found"));
        dodgeRepository.save(DodgeIndex.builder()
                .candleId(shouldNotBeFoundCandle.getId())
                .date(LocalDateTime.now().minus(Period.ofDays(5)))
                .figi("figi")
                .ticker("ticker2")
                .build());

        //when
        var result = dodgeRepository.getStocksByTickerAndDateBetween(shouldBeFoundDodge.getTicker(),
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        //then
        assertEquals(shouldBeFoundDodge.getId(), result.getId());
        assertEquals(shouldBeFoundDodge.getFigi(), result.getFigi());
        assertEquals(shouldBeFoundDodge.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")), result.getDate().toString());
        assertEquals(shouldBeFoundDodge.getCandleId(), result.getCandleId());
        assertEquals(shouldBeFoundDodge.getTicker(), result.getTicker());
    }

    @Test
    @DisplayName("test save")
    public void testSave() {

        var dodgeToSave = DodgeIndex.builder()
                .candleId("sdfaljva")
                .date(LocalDateTime.now())
                .figi("figu")
                .ticker("ticker3")
                .build();
        var result = dodgeRepository.save(dodgeToSave);
        result = dodgeRepository.findById(result.getId())
                .orElseThrow(() -> new AssertionError("dodge not found"));

        assertEquals(dodgeToSave.getId(), result.getId());
        assertEquals(dodgeToSave.getFigi(), result.getFigi());
        assertEquals(dodgeToSave.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")), result.getDate().toString());
        assertEquals(dodgeToSave.getCandleId(), result.getCandleId());
        assertEquals(dodgeToSave.getTicker(), result.getTicker());
    }

}
