package com.home.project.stocks.client;

import com.home.project.stocks.exceptions.TinkoffServerException;
import com.home.project.stocks.model.info.StockByTicker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SandBoxClientTestMock {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private final SandboxClient sandboxClient = new SandboxClient();

    @Test
    @DisplayName("Test server error - stocks")
    public void testGetStocksServerError() {
        sandboxClient.setStockListUrl("url");
        when(restTemplate.exchange(any(), any(), any(), eq(StockByTicker.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));
        assertThrows(TinkoffServerException.class, sandboxClient::getStocks);
    }

}
