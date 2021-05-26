package com.home.project.stocks.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DurationParserTest {

    @DisplayName("test normal min intervals")
    @ParameterizedTest
    @ValueSource(strings = {"1min", "2min", "3min", "5min", "10min", "15min", "30min"})
    void parseMinInterval(String input) {
        var number = Integer.parseInt(input.substring(0, input.indexOf("min")));
        var result = DurationParser.parseInterval(input);
        assertAll(() -> {
            assertTrue(result instanceof Duration);
            assertEquals(result, Duration.ofMinutes(number));
        });
    }

    @DisplayName("test hour intervals")
    @ParameterizedTest
    @ValueSource(strings = {"hour", "4hour"})
    void parseHourInterval(String input) {

        var result = DurationParser.parseInterval(input);
        assertAll(() -> {
            assertTrue(result instanceof Duration);
            assertEquals(result, input.contains("4") ? Duration.ofHours(4) : Duration.ofHours(1));
        });
    }

    @Test
    @DisplayName("test day interval")
    void parseDayInterval() {
        var result = DurationParser.parseInterval("day");
        assertAll(() -> {
            assertTrue(result instanceof Duration);
            assertEquals(result, Duration.ofDays(1));
        });
    }

    @Test
    @DisplayName("test week interval")
    void parseWeekInterval() {
        var result = DurationParser.parseInterval("week");
        assertAll(() -> {
            assertTrue(result instanceof Duration);
            assertEquals(result, Duration.ofDays(7));
        });
    }

    @Test
    @DisplayName("test illegal argument")
    void parseNotValidInterval() {
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parseInterval("5hour"));
    }
}