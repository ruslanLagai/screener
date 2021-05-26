package com.home.project.stocks.utils;

import com.home.project.stocks.model.candles.Interval;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

public class DurationParser {

    public static TemporalAmount parseInterval(String interval) {
        var enumVal = Interval.parse(interval);
        Duration duration = null;
        if (enumVal.getPeriod().contains("min")) {
            var index = enumVal.getPeriod().indexOf("min");
            var number = Integer.parseInt(enumVal.getPeriod().substring(0, index));
            duration = Duration.ofMinutes(number);
        } else if (enumVal.getPeriod().equals("hour")) {
            duration = Duration.ofHours(1);
        } else if (enumVal.getPeriod().contains("4hour")) {
            duration = Duration.ofHours(4);
        } else if (enumVal.getPeriod().contains("day")) {
            duration = Duration.ofDays(1);
        } else if (enumVal.getPeriod().contains("week")) {
            duration = Duration.ofDays(7);
        }
        return duration;
    }
}
