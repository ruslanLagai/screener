package com.home.project.stocks.scheduled;

import com.home.project.stocks.repository.CandleRepository;
import com.home.project.stocks.repository.DodgeRepository;
import com.home.project.stocks.repository.HammerRepository;
import com.home.project.stocks.repository.IndicatorRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.home.project.stocks.utils.Profiles.TEST_PROFILE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

//@ExtendWith(SpringExtension.class)
//@ActiveProfiles(TEST_PROFILE)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Testcontainers
class CandlesRequesterTest {

    @Autowired
    CandlesRequester candlesRequester;

    @Autowired
    IndicatorRepository indicatorRepository;

    @Autowired
    CandleRepository candleRepository;

    @Autowired
    HammerRepository hammerRepository;

    @Autowired
    DodgeRepository dodgeRepository;

    @Container
    public static ElasticsearchContainer elasticsearchContainer =
            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.13.0");

    @DynamicPropertySource
    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.rest.uris", elasticsearchContainer::getHttpHostAddress);
    }


//    @Test
    @DisplayName("test getting all stocks")
    void requestData() {
        candlesRequester.requestData();

        var indicators = indicatorRepository.findAll();
        var candles = candleRepository.findAll();
        var dodges = dodgeRepository.findAll();
        var hammers = hammerRepository.findAll();

        assertAll(() -> {
            assertTrue(indicators.iterator().hasNext());
        });
    }
}