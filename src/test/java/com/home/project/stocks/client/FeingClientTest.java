package com.home.project.stocks.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.processor.AbstractProcessorTest;
import com.home.project.stocks.processor.StocksProcessor;
import org.junit.Ignore;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Class to test feing client
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = FeingClientTest.Config.class)
@EnableConfigurationProperties
@Ignore("unused for now")
public class FeingClientTest extends AbstractProcessorTest {

    @Autowired
    WireMockServer wireMockServer;

    @Autowired
    NotifierClient notifierClient;

    public void setUp() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/notify"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));
    }


    public void testFeing() {
        var procResult = new ProcessingResult();
        procResult.setFigi("figi");
        procResult.setIsDodge(true);
        MultiValueMap<StocksProcessor.Processors, Candle> map = new LinkedMultiValueMap<>();
        map.put(StocksProcessor.Processors.DODGE, Collections.singletonList(generateCandle(0, 1, 2, 3, 4)));
        procResult.setProcessedCandles(map);

        var response = notifierClient.notifyUser(
                Collections.singletonList(procResult));
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @TestConfiguration
    static class Config {

        @Autowired
        private WireMockServer wireMockServer;

        @Bean(initMethod = "start", destroyMethod = "stop")
        public WireMockServer mockBooksService() {
            return new WireMockServer(8070);
        }
    }
}
