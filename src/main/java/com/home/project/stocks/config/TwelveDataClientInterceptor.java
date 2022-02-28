package com.home.project.stocks.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;

/**
 * Interceptor to add authorization header
 */
public class TwelveDataClientInterceptor implements RequestInterceptor {

    @Value("${twelvedata.api.key}")
    private String token;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.query("apikey", token);
    }
}
