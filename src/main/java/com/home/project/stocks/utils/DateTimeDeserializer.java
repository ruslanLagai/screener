package com.home.project.stocks.utils;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom date deserializer need to parse date with & w/o time
 */
public class DateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    public DateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    protected DateTimeDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        var formatter = p.getText().contains(":") ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd";
        return p.getText().contains(":") ? LocalDateTime.parse(p.getText(), DateTimeFormatter.ofPattern(formatter)).plusHours(8)
                : LocalDate.parse(p.getText(), DateTimeFormatter.ofPattern(formatter)).atTime(23, 59);
    }
}
