package com.home.project.stocks.model.processing;

import com.home.project.stocks.model.aplha.vantage.EmaPeriod;
import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.processor.DodgeProcessor;
import com.home.project.stocks.processor.HammerProcessor;
import com.home.project.stocks.processor.PatternProcessor;
import lombok.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to collect processing results from patterns and indicators
 */
@Data
public class ProcessingResult {

    private static Map<Class<? extends PatternProcessor>, String> stocksProcessorMap = Map.of(
            DodgeProcessor.class, "isDodge",
            HammerProcessor.class, "isHammer"
    );

    private String figi;
    private Boolean isDodge;
    private Boolean isHammer;
    private MultiValueMap<PatternProcessor.Processors, Candle> processedCandles = new LinkedMultiValueMap<>();
    private Map<EmaPeriod, EmaData> emaValue;
    private List<Double> macdBarValues;
    private List<Double> rsiValues;
    private double minPrice;
    private double closePrice;
    private double openPrice;
    private double maxPrice;
    private double volume;
    private Trend macdSignalTrend;
    private Trend macdBarTrend;
    private RsiSign rsiSign;
    private String ticker;

    public ProcessingResult() {
        emaValue = new HashMap<>();
        macdBarValues = new ArrayList<>();
    }

    @SneakyThrows
    public void initField(boolean value, PatternProcessor patternProcessor) {
        var field = stocksProcessorMap.get(patternProcessor.getClass());
        this.getClass().getDeclaredField(field).set(this, value);
    }

    @Data
    @Builder
    public static class EmaData {
        private boolean isCloseToEma;
        private double emaValue;
        private double difference;
        private LevelType levelType;
    }

    public enum LevelType {
        SUPPORT,
        RESISTANCE
    }

    public enum Trend {
        ASCENDING,
        DESCENDING,
        NO_SIGN
    }

    public enum RsiSign {
        OVERSOLD,
        OVERBOUGHT,
        NO_SIGN
    }
}
