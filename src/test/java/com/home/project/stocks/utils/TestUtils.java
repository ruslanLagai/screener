package com.home.project.stocks.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.home.project.stocks.model.candles.TwelveDataCandles;

import java.io.File;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author rlagay
 */
public class TestUtils {

    public static <T> T readValue(String resourcePath, Class<T> clazz) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        mapper.registerModule(new JavaTimeModule());
        return mapper.readValue(new File(resourcePath), clazz);
    }

    public static TwelveDataCandles readCandles(String file) {
        TwelveDataCandles candles = null;
        var content = TestUtils.class.getClassLoader().getResource(file);
        try {
            candles = readValue(content.getPath(), TwelveDataCandles.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return candles;
    }

    public static <T> T readData(String file, Class<T> cl) {
        var content = TestUtils.class.getClassLoader().getResource(file);
        try {
            return readValue(content.getPath(), cl);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            return null;
        }
    }

}
