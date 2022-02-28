package com.home.project.stocks.telegram;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum ChatType {
    PRIVATE("private");

    private final String type;

    public static ChatType parse(String value) {
        return Stream.of(values())
                .filter(type -> value.equalsIgnoreCase((type.getType())))
                .findFirst().orElse(null);
    }
}
