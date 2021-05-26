package com.home.project.stocks.client;

import java.net.URI;
import java.net.URISyntaxException;

import com.home.project.stocks.exceptions.*;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.home.project.stocks.model.candles.*;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SandboxClient implements TinkoffRestClient {

    private RestTemplate restTemplate;

    @Value("${sandbox.url.stocks}")
    private String stockListUrl;

    @Value("${sandbox.url.stockByTicker}")
    private String stockByTickerUrl;

    @Value("${sandbox.url.candesByFigi}")
    private String candesByFigi;

    @Value("${token}")
    private String token;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ResponseEntity<StockByTicker> getStocks() {
        ResponseEntity<StockByTicker> response = null;
        try {
            response = restTemplate.exchange(URI.create(stockListUrl), HttpMethod.GET,
                    getHttpEntity(), StockByTicker.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                //todo refresh token
                response = restTemplate.exchange(URI.create(stockListUrl), HttpMethod.GET,
                        getHttpEntity(), StockByTicker.class);
            } else {
                throw new BadRequestException(e.getStatusCode(), e.getStatusText());
            }
        } catch (HttpServerErrorException e) {
            log.error("Server error: ", e);
            throw new TinkoffServerException(e.getStatusCode(), e.getStatusText());
        }
        return response;
    }

    @Override
    public ResponseEntity<StockByTicker> getStockByTicker(String ticker) {
        ResponseEntity<StockByTicker> response = null;
        URI url = null;
        try {
            url = new URIBuilder(stockByTickerUrl)
                    .addParameter("ticker", ticker)
                    .build();
            response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), StockByTicker.class);
        } catch (URISyntaxException e) {
            log.error("Failed to parse query parameters. ", e);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                //todo refresh token
                assert url != null;
                response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), StockByTicker.class);
            } else {
                throw new BadRequestException(e.getStatusCode(), e.getStatusText());
            }
        } catch (HttpServerErrorException e) {
            log.error("Server error: ", e);
            throw new TinkoffServerException(e.getStatusCode(), e.getStatusText());
        }
       return response;
    }

    @Override
    public ResponseEntity<CandlesByFigi> getCandles(String figi, DateTime from, DateTime to, Interval interval) {
        ResponseEntity<CandlesByFigi> response = null;
        URI url = null;
        try {
            url = new URIBuilder(candesByFigi)
                    .addParameter("figi", figi)
                    .addParameter("from", from.toString())
                    .addParameter("to", to.toString())
                    .addParameter("interval", interval.toString())
                    .build();
            response = url != null ? restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), CandlesByFigi.class)
                    : null;
        } catch (URISyntaxException e) {
            log.error("Failed to parse query parameters. ", e);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                //todo refresh token
                response = url != null
                        ? restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), CandlesByFigi.class)
                        : null;
            } else {
                log.error("Invalid request parameters: from: {}, \nto: {} \ninterval: {}", from, to, interval);
                throw new BadRequestException(e.getStatusCode(), e.getStatusText());
            }
        } catch (HttpServerErrorException e) {
            log.error("Server error: ", e);
            throw new TinkoffServerException(e.getStatusCode(), e.getStatusText());
        }
        return response;
    }

    private HttpEntity getHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        return new HttpEntity(headers);
    }

    public void setStockListUrl(String stockListUrl) {
        this.stockListUrl = stockListUrl;
    }

}
