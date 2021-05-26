package com.home.project.stocks.utils;

import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class DateTimeDeserializer extends StdDeserializer<DateTime> {

    private static final DateTime FORMAT = new DateTime("yyyy-MM-dd'T'hh:mm:ss");

    protected DateTimeDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public DateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return DateTime.parse(p.getText());
    }
}
