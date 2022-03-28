package com.home.project.stocks.config;

import com.home.project.stocks.config.properties.TwelveDataApiProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;

import java.util.Random;

/**
 * Interceptor to add authorization header
 */
@RequiredArgsConstructor
public class TwelveDataClientInterceptor implements RequestInterceptor {

    private final TwelveDataApiProperties twelveDataApiProperties;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        var random= new Random();
        var index  = random.ints(0, 3)
                .findFirst()
                .getAsInt();
        requestTemplate.query("apikey", twelveDataApiProperties.getKey().get(index));
    }
}
