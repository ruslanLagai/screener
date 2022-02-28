package com.home.project.stocks.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.home.project.stocks.model.aplha.vantage.Interval;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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
    private boolean isDodge;
    private boolean isHammer;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    public static Candle populateFields(com.home.project.stocks.model.candles.Candle candle, String ticker, String figi,
                                        boolean isDodge, boolean isHammer) {
        return Candle.builder()
                .open(candle.getO())
                .close(candle.getC())
                .high(candle.getH())
                .low(candle.getL())
                .volume(candle.getV())
                .time(candle.getDatetime())
                .interval(candle.getInterval())
                .ticker(ticker)
                .figi(figi)
                .isHammer(isHammer)
                .isDodge(isDodge)
                .build();
    }
}
