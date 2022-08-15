package com.home.project.stocks.mapper;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.entity.DailyCandle;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author rlagay
 */
class CandlesMapperTest {

    private static final LocalDateTime DATE_TIME = LocalDateTime.of(2022, 2, 2, 10, 0, 0);
    private final CandlesMapper mapper = Mappers.getMapper(CandlesMapper.class);

    @Test
    void toRestCandle() {
        var result = mapper.toRestCandle(mockDailyCandle());
        assertAll(() -> {
            assertEquals(10.0, result.getH());
            assertEquals(5.0, result.getL());
            assertEquals(9.0, result.getC());
            assertEquals(8.0, result.getO());
            assertEquals(40.0, result.getV());
            assertNull(result.getFigi());
            assertEquals(DATE_TIME, result.getDatetime());
            assertEquals("1day", result.getInterval());
        });
    }

    @Test
    void toRestCandleNullValues() {
        var result = mapper.toRestCandle(mockPartDailyCandle());
        assertAll(() -> {
            assertEquals(0.0, result.getH());
            assertEquals(0.0, result.getL());
            assertEquals(9.0, result.getC());
            assertEquals(8.0, result.getO());
            assertEquals(40.0, result.getV());
            assertNull(result.getFigi());
            assertEquals(DATE_TIME, result.getDatetime());
            assertNull(result.getInterval());

        });
    }

    @Test
    void toDbCandle() {
        var result = mapper.toDbCandle(mockCandle(), "AAPL");
        assertAll(() -> {
            assertEquals(15.0, result.getHigh());
            assertEquals(10.0, result.getLow());
            assertEquals(10.0, result.getOpen());
            assertEquals(12.0, result.getClose());
            assertEquals(30.0, result.getVolume());
            assertEquals("AAPL", result.getTicker());
            assertEquals(DATE_TIME, result.getTime());
            assertEquals("1day", result.getInterval());
        });
    }

    private Candle mockCandle() {
        return Candle.builder()
                .o(10.0)
                .c(12.0)
                .h(15.0)
                .l(10.0)
                .v(30.0)
                .interval("1day")
                .datetime(DATE_TIME)
                .build();
    }

    private DailyCandle mockDailyCandle() {
        return DailyCandle.builder()
                .time(DATE_TIME)
                .high(10.0)
                .low(5.0)
                .close(9.0)
                .open(8.0)
                .ticker("AAPL")
                .interval("1day")
                .volume(40)
                .id(1L)
                .build();
    }

    private DailyCandle mockPartDailyCandle() {
        return DailyCandle.builder()
                .time(DATE_TIME)
                .close(9.0)
                .open(8.0)
                .volume(40)
                .id(1L)
                .build();
    }

}