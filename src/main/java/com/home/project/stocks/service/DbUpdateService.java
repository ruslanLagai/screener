package com.home.project.stocks.service;

import com.home.project.stocks.model.entity.Candle;
import com.home.project.stocks.model.entity.DailyCandle;
import com.home.project.stocks.model.entity.DailyIndicator;
import com.home.project.stocks.model.entity.ProcessedLevels;
import com.home.project.stocks.model.processing.ProcessingResult;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Set;

/**
 * @author rlagay
 */
public interface DbUpdateService {
    void updateEmaOnDailyIndicator(DailyIndicator indicator);
    void updateMacdOnDailyIndicator(DailyIndicator indicator);
    void updateRsiOnDailyIndicator(DailyIndicator indicator);
    void savePattern(Candle candle);
    void activateTelegramChat(Update update);
    void stopTelegramChat(Update update);
    void saveDailyCandle(Set<DailyCandle> candles);
    void saveIndicatorData(ProcessingResult processingResult);
    void saveWeeklyLevels(String ticker, Set<Double> doubles);
    void saveProcessedLevels(ProcessedLevels processedLevels);
}
