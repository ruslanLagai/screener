package com.home.project.stocks.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.*;

/**
 * Converter to parse date from elastic
 */
@ReadingConverter
public class LongToDateTimeConverter implements Converter<Long, LocalDateTime> {

    public static String zoneOffset;

    public LongToDateTimeConverter() {
        zoneOffset = Clock.systemDefaultZone().getZone().getRules().getOffset(Instant.now()).getId();
    }

    @Override
    public LocalDateTime convert(Long source) {
        return LocalDateTime.ofEpochSecond(source, 0, ZoneOffset.of(zoneOffset));
    }
}
