package com.home.project.stocks.model.aplha.vantage;

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
    FIVE_MIN("5min"),
    FIFTEEN_MIN("15min"),
    THIRTY_MIN("30min"),
    SIXTY_MIN("60min"),
    ONE_DAY("daily"),
    ONE_WEEK("weekly"),
    TWELVE_DATA_ONE_HOUR("1h"),
    TWELVE_DATA_FOUR_HOUR("4h"),
    TWELVE_DATA_ONE_DAY("1day"),
    TWELVE_DATA_ONE_WEEK("1week"),
    ONE_MONTH("monthly");

    private final String interval;

    @JsonCreator
    public static Interval parse(String value) {
        return Stream.of(values())
                .filter(period -> Objects.equals(value, period.toString()))
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    @JsonValue
    public String toString() {
        return interval;
    }
}
