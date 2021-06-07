package com.home.project.stocks.repository;

import com.home.project.stocks.model.repositories.CandleIndex;
import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DisplayName("Test candle repo")
public class CandleRepositoryTest extends AbstractRepositoryTest {

    @Test
    @DisplayName("test getById")
    public void testSave() throws IOException {
        var candle = generateCandle(1, 2, 3, 4, 5);
        candle.setFigi("figi");
        candle.setInterval("day");
        candle.setTime(LocalDateTime.of(2021, 6, 14, 1,32,44));

        var client = RestClient.builder(HttpHost.create(container.getHttpHostAddress())).build();
        client.performRequest(new Request(HttpMethod.GET.name(), "_cat/indices"));
        var candleToSave = CandleIndex.populateFields(candle);
        var result = candleRepository.save(candleToSave);
        result = candleRepository.findById(result.getId())
                .orElseThrow(() -> new AssertionError("candle is not found"));

        assertEquals(candleToSave.getId(), result.getId());
        assertEquals(candleToSave.getFigi(), result.getFigi());
        assertEquals(candleToSave.getC(), result.getC());
        assertEquals(candleToSave.getH(), result.getH());
        assertEquals(candleToSave.getL(), result.getL());
        assertEquals(candleToSave.getO(), result.getO());
        assertEquals(candleToSave.getInterval(), result.getInterval());
        assertEquals(candleToSave.getTime(), result.getTime());
    }

}
