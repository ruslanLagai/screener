package com.home.project.stocks.model.candles;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public enum Interval {
    ONE_MIN("1min"),
    TWO_MIN("2min"),
    THREE_MIN("3min"),
    FIVE_MIN("5min"),
    TEN_MIN("10min"),
    FIFTEEN_MIN("15min"),
    THIRTY_MIN("30min"),
    ONE_HOUR("hour"),
    FOUR_HOUR("4hour"),
    ONE_DAY("day"),
    ONE_WEEK("week"),
    ONE_MONTH("month");

    private final String period;

    @JsonCreator
    public static Interval parse(String value) {
        return Stream.of(values())
                .filter(period -> Objects.equals(value, period.toString()))
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    @JsonValue
    public String toString() {
        return period;
    }
}


