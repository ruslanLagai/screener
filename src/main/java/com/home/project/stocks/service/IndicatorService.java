package com.home.project.stocks.service;

import com.home.project.stocks.model.aplha.vantage.EmaPeriod;
import com.home.project.stocks.model.aplha.vantage.Interval;
import com.home.project.stocks.model.aplha.vantage.RsiPeriod;
import com.home.project.stocks.model.aplha.vantage.SeriesType;
import com.home.project.stocks.model.indicators.ParsedIndicator;

/**
 * Interface to interact with Alpha Vantage to get indicator data
 */
public interface IndicatorService extends Retiable {

    ParsedIndicator getEma(String ticker, Interval interval, EmaPeriod emaPeriod, SeriesType seriesType);

    ParsedIndicator getRsi(String ticker, Interval interval, RsiPeriod rsiPeriod, SeriesType seriesType);

    ParsedIndicator getMacd(String ticker, Interval interval, SeriesType seriesType);
}
