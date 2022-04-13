package com.home.project.stocks.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.math3.util.Precision;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * Candle with buy/sell signal
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "candles")
public class Candle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String ticker;
    @Column
    private String figi;
    @Column(name = "candle_interval", nullable = false)
    private String interval;
    private double open;
    private double close;
    private double high;
    private double low;
    private double volume;
    private boolean isEngulfing;
    private boolean isHammer;

    @Override
    public String toString() {
        var isEngulfing = this.isEngulfing ? "\ud83d\ude80" : "⛔";
        var isHammer = this.isHammer ? "\uD83D\uDED1" : "⛔";

        return "Тикер: " + ticker + "\n" +
                "Цена открытия: " + open + "\n" +
                "Цена закрытия: " + close + "\n" +
                "Макс цена: " + high + "\n" +
                "Мин цена: " + low + "\n" +
                "Бычье поглощение: " + isEngulfing + "\n" +
                "Паттерн молот = " + isHammer;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    public static Candle populateFields(com.home.project.stocks.model.candles.Candle candle, String ticker, String figi,
                                        boolean isEngulfing, boolean isHammer) {
        return Candle.builder()
                .open(Precision.round(candle.getO(), 2))
                .close(Precision.round(candle.getC(), 2))
                .high(Precision.round(candle.getH(), 2))
                .low(Precision.round(candle.getL(), 2))
                .volume(Precision.round(candle.getV(), 2))
                .time(candle.getDatetime())
                .interval(candle.getInterval())
                .ticker(ticker)
                .figi(figi)
                .isHammer(isHammer)
                .isEngulfing(isEngulfing)
                .build();
    }
}
