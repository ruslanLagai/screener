package com.home.project.stocks.service.impl;

import com.home.project.stocks.model.entity.Candle;
import com.home.project.stocks.model.entity.DailyCandle;
import com.home.project.stocks.model.entity.DailyIndicator;
import com.home.project.stocks.model.entity.Levels;
import com.home.project.stocks.model.entity.ProcessedIndicators;
import com.home.project.stocks.model.entity.ProcessedLevels;
import com.home.project.stocks.model.entity.TelegramChatEntity;
import com.home.project.stocks.model.entity.WeeklyLevel;
import com.home.project.stocks.model.processing.ProcessingResult;
import com.home.project.stocks.model.telegram.ChatStatus;
import com.home.project.stocks.repository.CandleRepository;
import com.home.project.stocks.repository.ChatRepository;
import com.home.project.stocks.repository.DailyCandleRepository;
import com.home.project.stocks.repository.DailyEmaRepository;
import com.home.project.stocks.repository.DailyIndicatorDataRepository;
import com.home.project.stocks.repository.DailyMacdRepository;
import com.home.project.stocks.repository.DailyProcessedIndicatorRepository;
import com.home.project.stocks.repository.DailyRsiRepository;
import com.home.project.stocks.repository.ProcessedLevelsRepository;
import com.home.project.stocks.repository.WeeklyLevelsRepository;
import com.home.project.stocks.service.DbUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.exact;

/**
 * Service to populate db in separate thread
 *
 * @author rlagay
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DbUpdateServiceImpl implements DbUpdateService {

    private final DailyIndicatorDataRepository dailyIndicatorDataRepository;
    private final DailyEmaRepository dailyEmaRepository;
    private final DailyRsiRepository dailyRsiRepository;
    private final DailyMacdRepository dailyMacdRepository;
    private final CandleRepository candleRepository;
    private final ChatRepository chatRepository;
    private final DailyCandleRepository dailyCandleRepository;
    private final DailyProcessedIndicatorRepository indicatorRepository;
    private final WeeklyLevelsRepository weeklyLevelsRepository;
    private final ProcessedLevelsRepository processedLevelsRepository;
    private ExecutorService executorService;

    // customizing executorService, set different corePoolSize and maxPoolSize
    // to have more flexible service
    // newFixedThreadPool has unlimited queue -> could be a risk
    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(10);
        ((ThreadPoolExecutor) executorService).setCorePoolSize(5);
    }

    public void savePattern(Candle candle) {
        executorService.submit(() -> candleRepository.save(candle));
    }

    @Override
    public void activateTelegramChat(Update update) {
        var chatId = update.getMessage().getChatId();
        try {
            executorService.submit(() -> {
                if (!chatRepository.existsById(chatId)) {
                    chatRepository.save(TelegramChatEntity.builder()
                            .id(chatId)
                            .firstName(update.getMessage().getFrom().getFirstName())
                            .lastName(update.getMessage().getFrom().getLastName())
                            .userName(update.getMessage().getFrom().getUserName())
                            .status(ChatStatus.ACTIVE)
                            .build());
                } else {
                    chatRepository.findById(chatId).ifPresent(chatEntity -> {
                        chatEntity.setStatus(ChatStatus.ACTIVE);
                        chatRepository.save(chatEntity);
                    });
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to activate telegram chat with id {}", chatId, e);
        }
    }

    @Override
    public void stopTelegramChat(Update update) {
        var chatId = update.getMessage().getChatId();
        try {
            executorService.submit(() -> {
                if (chatRepository.existsById(chatId)) {
                    chatRepository.save(TelegramChatEntity.builder()
                            .id(chatId)
                            .firstName(update.getMessage().getFrom().getFirstName())
                            .lastName(update.getMessage().getFrom().getLastName())
                            .userName(update.getMessage().getFrom().getUserName())
                            .status(ChatStatus.STOPPED)
                            .build());
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to deactivate telegram chat with id {}", chatId, e);
        }
    }

    @Override
    public void saveDailyCandle(Set<DailyCandle> candles) {
        executorService.submit(() -> dailyCandleRepository.saveAll(candles));
    }

    @Override
    public void saveIndicatorData(ProcessingResult processingResult) {
        try {
            executorService.submit(() -> {
                var processedIndicator = ProcessedIndicators.populateFields(processingResult,
                        LocalDateTime.now());
                var isEmaSignal = processingResult.getEmaValue().values().stream()
                        .filter(ProcessingResult.EmaData::isCloseToEma)
                        .anyMatch(item -> !item.isCloseRetest());
                var isMacdSignal = ProcessingResult.Trend.NO_SIGN != processingResult.getMacdDivergence()
                        && processingResult.getMacdDivergence() != null;
                Stream.of(processingResult)
                        .filter(result -> isMacdSignal|| isEmaSignal)
                        .findAny()
                        .ifPresent(indicator -> indicatorRepository.save(processedIndicator));
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to save daily candles", e);
        }
    }

    @Override
    public void saveWeeklyLevels(String ticker, Set<Double> doubles) {
        if (doubles.isEmpty()) {
            return;
        }
        var weeklyLevel = WeeklyLevel.builder()
                .ticker(ticker)
                .build();
        var levels = doubles.stream()
                .map(value -> Levels.builder()
                        .weeklyLevel(weeklyLevel)
                        .value(value)

                        .build())
                .collect(Collectors.toSet());
        weeklyLevel.setLevels(levels);
        executorService.submit(() -> weeklyLevelsRepository.save(weeklyLevel));
    }

    @Override
    public void saveProcessedLevels(ProcessedLevels processedLevels) {
        try {
            executorService.submit(() -> processedLevelsRepository.save(processedLevels)).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to save daily candles", e);
        }
    }

    @Override
    public void updateEmaOnDailyIndicator(DailyIndicator indicator) {
        try {
            executorService.submit(() -> {
                var saved = dailyIndicatorDataRepository.getByTickerAndDateAndTimeframe(
                        indicator.getTicker(), indicator.getDate(), indicator.getTimeframe());
                if (saved != null) {
                    indicator.getEmaData().forEach(dailyEma ->
                            dailyEmaRepository.insertEmaData(dailyEma.getEmaType(), dailyEma.getEmaValue(),
                                    dailyEma.getDatetime(), saved.getId()));
                } else {
                    dailyIndicatorDataRepository.save(indicator);
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to save daily ema, " + e.getMessage(), e);
        }
    }

    @Override
    public void updateRsiOnDailyIndicator(DailyIndicator indicator) {
        try {
            executorService.submit(() -> {
                var saved = dailyIndicatorDataRepository.getByTickerAndDateAndTimeframe(
                        indicator.getTicker(), indicator.getDate(), indicator.getTimeframe());
                if (saved != null) {
                    indicator.getRsiData().forEach(dailyRsi ->
                            dailyRsiRepository.insertRsiData(dailyRsi.getRsiValue(), dailyRsi.getDatetime(), saved.getId()));
                } else {
                    dailyIndicatorDataRepository.save(indicator);
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to save daily rsi, " + e.getMessage(), e);
        }
    }

    public void updateMacdOnDailyIndicator(DailyIndicator indicator) {
        try {
            executorService.submit(() -> {
                var saved = dailyIndicatorDataRepository.getByTickerAndDateAndTimeframe(
                        indicator.getTicker(), indicator.getDate(), indicator.getTimeframe());
                if (saved != null) {
                    indicator.getMacdData().forEach(dailyMacd ->
                            dailyMacdRepository.insertMacdData(dailyMacd.getMacdHistValue(),
                                    dailyMacd.getMacdSignalValue(), dailyMacd.getMacdValue(),
                                    dailyMacd.getDatetime(), saved.getId()));
                } else {
                    dailyIndicatorDataRepository.save(indicator);
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to save daily rsi, " + e.getMessage(), e);
        }
    }



    private ExampleMatcher getCandleMatcher() {
        return ExampleMatcher.matching()
                .withMatcher("ticker", exact())
                .withMatcher("date", exact())
                .withMatcher("timeframe", exact());
    }

    @PreDestroy
    public void stop() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
