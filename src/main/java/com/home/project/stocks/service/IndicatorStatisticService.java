package com.home.project.stocks.service;

import com.home.project.stocks.model.api.Interval;
import com.home.project.stocks.model.processing.ProcessingResult;

/**
 * @author rlagay
 */
public interface IndicatorStatisticService {

    void analyzeStock(ProcessingResult processingResult, Interval interval);
}
