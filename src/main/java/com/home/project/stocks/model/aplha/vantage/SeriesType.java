package com.home.project.stocks.model.aplha.vantage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public enum SeriesType {
    OPEN("open"),
    CLOSE("close"),
    HIGH("high"),
    LOW("low");

    private final String period;

    @JsonCreator
    public static SeriesType parse(String value) {
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
