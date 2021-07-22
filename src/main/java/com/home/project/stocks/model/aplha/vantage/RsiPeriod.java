package com.home.project.stocks.model.aplha.vantage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public enum RsiPeriod {
    NINE("9"),
    FOURTEEN("14"),
    TWENTY_FOUR("24");

    private final String period;

    @JsonCreator
    public static RsiPeriod parse(String value) {
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
