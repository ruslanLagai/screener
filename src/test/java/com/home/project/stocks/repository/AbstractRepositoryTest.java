package com.home.project.stocks.repository;

import com.home.project.stocks.processor.AbstractProcessorTest;
import com.home.project.stocks.service.RepositoryService;
import com.home.project.stocks.utils.LongToDateTimeConverter;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.time.Duration;
import java.util.Collections;

@ContextConfiguration(classes = AbstractRepositoryTest.Config.class)
@TestConfiguration
public class AbstractRepositoryTest extends AbstractProcessorTest {

    protected static ElasticsearchContainer container;

    @Autowired
    CandleRepository candleRepository;

    @Autowired
    DodgeRepository dodgeRepository;

    @BeforeAll
    public static void setUp() {
        container = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.13.0");
        container.start();
    }

    @ComponentScan(basePackages = "com.home.project.stocks.repository")
    @EnableElasticsearchRepositories(basePackages = "com.home.project.stocks.repository")
    static class Config extends AbstractElasticsearchConfiguration {

        @Bean
        public RepositoryService repositoryService(CandleRepository candleRepository,
                                                   DodgeRepository dodgeRepository,
                                                   HammerRepository hammerRepository,
                                                   IndicatorRepository indicatorRepository) {
            return new RepositoryService(candleRepository, dodgeRepository, hammerRepository, indicatorRepository);
        }

        @Bean
        public RestHighLevelClient elasticsearchClient() {
            ClientConfiguration clientConfiguration
                    = ClientConfiguration.builder()
                    .connectedTo(container.getHttpHostAddress())
                    .build();
            return RestClients.create(clientConfiguration).rest();
        }

        @Bean(name = {"elasticsearchOperations", "elasticsearchTemplate"})
        public ElasticsearchOperations elasticsearchOperations() {
            return new ElasticsearchRestTemplate(elasticsearchClient());
        }


        @Bean
        @Override
        public ElasticsearchCustomConversions elasticsearchCustomConversions() {
            return new ElasticsearchCustomConversions(Collections.singleton(new LongToDateTimeConverter()));
        }

    }
}
