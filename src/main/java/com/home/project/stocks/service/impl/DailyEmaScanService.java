package com.home.project.stocks.service.impl;

import com.home.project.stocks.model.aplha.vantage.EmaPeriod;
import com.home.project.stocks.model.aplha.vantage.Interval;
import com.home.project.stocks.model.aplha.vantage.SeriesType;
import com.home.project.stocks.service.DailyScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to process ema
 *  200 on weekly TF
 *  10/20 on daily TF - Kell
 *
 * @author rlagay
 */
@Service
@Slf4j
public class DailyEmaScanService implements DailyScanService {

    private final DailyIndicatorService dailyIndicatorService;

    private final List<EmaPeriod> emaPeriods = List.of(EmaPeriod.TEN, EmaPeriod.TWENTY, EmaPeriod.TWO_HUNDRED);

    public DailyEmaScanService(DailyIndicatorService dailyIndicatorService) {
        this.dailyIndicatorService = dailyIndicatorService;
    }

    @Override
    public void processStock(String ticker, String figi) {
//        emaPeriods.stream().
//                filter(emaPeriod -> !isKellStrategy(emaPeriod))
//                .forEach(emaPeriod -> {
//                    var ema = dailyIndicatorService.getEma(ticker, Interval.TWELVE_DATA_ONE_WEEK, emaPeriod, SeriesType.CLOSE);
//
//                });
    }

    private boolean isKellStrategy(EmaPeriod emaPeriod) {
        return EmaPeriod.TEN.equals(emaPeriod) || EmaPeriod.TWENTY.equals(emaPeriod);
    }
}
