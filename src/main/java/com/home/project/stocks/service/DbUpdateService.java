package com.home.project.stocks.service;

import com.home.project.stocks.model.entity.Candle;
import com.home.project.stocks.model.entity.DailyIndicator;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ExecutionException;

/**
 * @author rlagay
 */
public interface DbUpdateService {
    void updateEmaOnDailyIndicator(DailyIndicator indicator);
    void updateRsiOnDailyIndicator(DailyIndicator indicator);
    void savePattern(Candle candle);
    void activateTelegramChat(Update update);
    void stopTelegramChat(Update update) throws ExecutionException, InterruptedException;
}
