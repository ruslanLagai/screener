package com.home.project.stocks;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@PropertySource("classpath:sandbox.properties")
@EnableScheduling
public class StocksApplication {

    public static void main(String[] args) {
        var builder = new SpringApplicationBuilder();
        builder.bannerMode(Banner.Mode.CONSOLE)
                .profiles("sandbox")
                .build()
                .run(args);
    }

}
