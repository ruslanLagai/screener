package com.home.project.stocks.model.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public enum Function {
    EMA("EMA"),
    MACD("MACD"),
    RSI("RSI"),
    TIME_SERIES_DAILY("TIME_SERIES_DAILY_ADJUSTED");

    private final String indicator;

    @JsonCreator
    public static Function parse(String value) {
        return Stream.of(values())
                .filter(period -> Objects.equals(value, period.toString()))
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    @JsonValue
    public String toString() {
        return indicator;
    }
}
