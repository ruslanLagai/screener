package com.home.project.stocks.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Feign client config
 */
@Configuration
public class TwelveDataFeignConfig {
    @Bean
    Logger.Level twelveDataLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    Retryer retryer() {
        return new Retryer.Default(5000, SECONDS.toMillis(25), 5);
    }

    @Bean
    ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    RequestInterceptor clientInterceptor() {
        return new TwelveDataClientInterceptor();
    }

}
