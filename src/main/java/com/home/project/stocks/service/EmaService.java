package com.home.project.stocks.service;

import com.home.project.stocks.client.AlphaVantageApiClient;
import com.home.project.stocks.model.aplha.vantage.*;
import com.home.project.stocks.model.indicators.ParsedEma;
import com.home.project.stocks.parser.AlphaVantageEmaParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmaService implements IndicatorService {

    private AlphaVantageApiClient alphaVantageApiClient;
    @Value("${alpha.vantage.key}")
    private String key;

    @Autowired
    public void setAlphaVantageApiClient(AlphaVantageApiClient alphaVantageApiClient) {
        this.alphaVantageApiClient = alphaVantageApiClient;
    }

    protected ParsedEma getEma(String ticker, Interval interval, EmaPeriod emaPeriod, SeriesType seriesType) {
        var ema = alphaVantageApiClient.getEma(Indicator.EMA.getIndicator(), ticker,
                interval.getInterval(), emaPeriod.getPeriod(), seriesType.getPeriod(), key);
        return AlphaVantageEmaParser.parseVantageEma(ema);
    }
}
