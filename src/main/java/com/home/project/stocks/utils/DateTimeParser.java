package com.home.project.stocks.utils;

import lombok.extern.log4j.Log4j2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Log4j2
public class DateTimeParser {
    public static Date parseDate(String date) {
        var formatter = date.contains(":") ?
                new SimpleDateFormat("yyyy-MM-dd hh:mm") :
                new SimpleDateFormat("yyyy-MM-dd");
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            log.error("Failed to parse date in response. " + e.getMessage());
        }
        return null;
    }
}
