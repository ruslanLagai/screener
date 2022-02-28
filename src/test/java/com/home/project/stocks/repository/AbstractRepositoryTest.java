package com.home.project.stocks.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.home.project.stocks.config.DbConfig;
import com.home.project.stocks.helpers.YamlPropertySourceFactory;
import com.home.project.stocks.processor.AbstractProcessorTest;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class AbstractRepositoryTest extends AbstractProcessorTest {

    @Container
    protected static MySQLContainer<?> container = new MySQLContainer<>("mysql:8");


    @TestConfiguration
    @ComponentScan(basePackages = "com.home.project.stocks.repository")
    @EnableJpaRepositories(basePackages = "com.home.project.stocks.repository")
    @Import(value = {DbConfig.class, DataSourceAutoConfiguration.class})
    @PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
    public static class Config implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Bean
        public ObjectMapper objectMapper() {
            var mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            return mapper;
        }

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            final var values =
                    TestPropertyValues.of(
                            "spring.datasource.driver-class-name=" + container.getDriverClassName(),
                            "spring.datasource.url=" + container.getJdbcUrl(),
                            "spring.datasource.username=" + container.getUsername(),
                            "spring.datasource.password=" + container.getPassword(),
                            "spring.datasource.type=com.mysql.cj.jdbc.MysqlConnectionPoolDataSource"
                    );
            values.applyTo(configurableApplicationContext);
        }
    }
}
