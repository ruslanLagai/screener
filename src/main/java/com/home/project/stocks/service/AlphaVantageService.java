package com.home.project.stocks.service;

import com.home.project.stocks.client.AlphaVantageApiClient;
import com.home.project.stocks.exceptions.IndicatorParsingException;
import com.home.project.stocks.model.aplha.vantage.*;
import com.home.project.stocks.model.indicators.ParsedIndicator;
import com.home.project.stocks.parser.AlphaVantageParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to get data from Alpha Vantage
 */
@Service
public class AlphaVantageService implements IndicatorService {

    private AlphaVantageApiClient alphaVantageApiClient;
    @Value("${alpha.vantage.key}")
    private String key;

    @Autowired
    public void setAlphaVantageApiClient(AlphaVantageApiClient alphaVantageApiClient) {
        this.alphaVantageApiClient = alphaVantageApiClient;
    }

    @Override
    public ParsedIndicator getEma(String ticker, Interval interval, EmaPeriod emaPeriod, SeriesType seriesType) {
        var ema = alphaVantageApiClient.getEma(Function.EMA.getIndicator(), ticker,
                interval.getInterval(), emaPeriod.getPeriod(), seriesType.getPeriod(), key);
        if (ema == null) {
            throw new IndicatorParsingException(String.format("Retrieved null response on ema, ticker %s", ticker));
        }
        return AlphaVantageParser.parseIndicator(ema);
    }

    @Override
    public ParsedIndicator getRsi(String ticker, Interval interval, RsiPeriod rsiPeriod, SeriesType seriesType) {
        var rsi = alphaVantageApiClient.getRsi(Function.RSI.getIndicator(), ticker,
                interval.getInterval(), rsiPeriod.getPeriod(), seriesType.getPeriod(), key);
        if (rsi == null) {
            throw new IndicatorParsingException(String.format("Retrieved null response on rsi, ticker %s", ticker));
        }
        return AlphaVantageParser.parseIndicator(rsi);
    }

    @Override
    public ParsedIndicator getMacd(String ticker, Interval interval, SeriesType seriesType) {
        var macd = alphaVantageApiClient.getMacd(Function.MACD.getIndicator(), ticker,
                interval.getInterval(), seriesType.getPeriod(), key);
        if (macd == null) {
            throw new IndicatorParsingException(String.format("Retrieved null response on macd, ticker %s", ticker));
        }
        return AlphaVantageParser.parseMacd(macd);
    }

    @Override
    public Candles getDailyCandles(String ticker) {
        var candles = alphaVantageApiClient.getCandles(Function.TIME_SERIES_DAILY.getIndicator(),
                ticker, key);
        if (candles == null) {
            throw new IndicatorParsingException(String.format("Retrieved null response on candles, ticker %s", ticker));
        }
        return candles;
    }
}
