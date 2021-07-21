package com.home.project.stocks.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Logger;
import feign.Retryer;
import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static java.util.concurrent.TimeUnit.SECONDS;

@Configuration
public class FeingConfig {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    Retryer retryer() {
        return new Retryer.Default(100, SECONDS.toMillis(1), 3);
    }

}
