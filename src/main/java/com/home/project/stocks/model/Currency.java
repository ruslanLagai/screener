package com.home.project.stocks.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public enum Currency {
    USD("USD"),
    EURO("EUR"),
    RUB("RUB");

    private final String currency;

    @JsonCreator
    public static Currency parse(String value) {
        return Stream.of(values())
                .filter(currency -> Objects.equals(value, currency.toString()))
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    @Override
    @JsonValue
    public String toString() {
        return currency;
    }
}


