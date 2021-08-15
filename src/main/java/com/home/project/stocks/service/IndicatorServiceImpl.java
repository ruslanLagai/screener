package com.home.project.stocks.service;

import com.home.project.stocks.client.AlphaVantageApiClient;
import com.home.project.stocks.model.aplha.vantage.*;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.parser.AlphaVantageParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class IndicatorServiceImpl implements IndicatorService {

    private AlphaVantageApiClient alphaVantageApiClient;
    @Value("${alpha.vantage.key}")
    private String key;

    @Autowired
    public void setAlphaVantageApiClient(AlphaVantageApiClient alphaVantageApiClient) {
        this.alphaVantageApiClient = alphaVantageApiClient;
    }

    public ParsedIndicator getEma(String ticker, Interval interval, EmaPeriod emaPeriod, SeriesType seriesType) {
        var ema = alphaVantageApiClient.getEma(Indicator.EMA.getIndicator(), ticker,
                interval.getInterval(), emaPeriod.getPeriod(), seriesType.getPeriod(), key);
        return AlphaVantageParser.parseIndicator(ema);
    }

    public ParsedIndicator getRsi(String ticker, Interval interval, RsiPeriod rsiPeriod, SeriesType seriesType) {
        var rsi = alphaVantageApiClient.getRsi(Indicator.RSI.getIndicator(), ticker,
                interval.getInterval(), rsiPeriod.getPeriod(), seriesType.getPeriod(), key);
        return AlphaVantageParser.parseIndicator(rsi);
    }

    public ParsedIndicator getMacd(String ticker, Interval interval, SeriesType seriesType) {
        var macd = alphaVantageApiClient.getMacd(Indicator.MACD.getIndicator(), ticker,
                interval.getInterval(), seriesType.getPeriod(), key);
        return AlphaVantageParser.parseMacd(macd);
    }
}
