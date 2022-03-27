package com.home.project.stocks.model.candles;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.home.project.stocks.utils.DateTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candle {
    private String figi;
    private String interval;
    @JsonAlias("open")
    private double o;
    @JsonAlias("close")
    private double c;
    @JsonAlias("high")
    private double h;
    @JsonAlias("low")
    private double l;
    @JsonAlias("volume")
    private double v;
    @JsonDeserialize(using = DateTimeDeserializer.class)
    private LocalDateTime datetime;
}
