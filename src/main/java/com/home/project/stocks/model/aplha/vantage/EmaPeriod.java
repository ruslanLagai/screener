package com.home.project.stocks.model.aplha.vantage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public enum EmaPeriod {
    TEN("10"),
    TWENTY("20"),
    FIFTY("50"),
    ONE_HUNDRED("100"),
    TWO_HUNDRED("200"),
    ONE_THOUSAND("1000");

    private final String period;

    @JsonCreator
    public static EmaPeriod parse(String value) {
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
