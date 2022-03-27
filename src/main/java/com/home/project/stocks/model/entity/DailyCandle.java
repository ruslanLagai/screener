package com.home.project.stocks.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.home.project.stocks.model.api.Interval;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * Daily candles should be cleared at the end of the day
 *
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "daily_candles")
public class DailyCandle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String ticker;

    private double open;
    private double close;
    private double high;
    private double low;
    private double volume;
    @Column(name = "candle_interval", nullable = false)
    private String interval;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    public static com.home.project.stocks.model.candles.Candle toRestCandle(DailyCandle candle) {
        var c = new com.home.project.stocks.model.candles.Candle();
        c.setV(candle.getVolume());
        c.setO(candle.getOpen());
        c.setL(candle.getLow());
        c.setH(candle.getHigh());
        c.setDatetime(candle.getTime());
        c.setInterval(candle.getInterval());
        return c;
    }

    public static DailyCandle toDbCandle(com.home.project.stocks.model.candles.Candle candle, String ticker) {
        return DailyCandle.builder()
                .open(candle.getO())
                .close(candle.getC())
                .high(candle.getH())
                .low(candle.getL())
                .volume(candle.getV())
                .interval(Interval.TWELVE_DATA_ONE_DAY.getInterval())
                .ticker(ticker)
                .time(candle.getDatetime())
                .build();
    }
}
