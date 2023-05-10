package com.home.project.stocks.model.processing;

import com.home.project.stocks.model.api.EmaPeriod;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to collect processing results from patterns and indicators
 */
@Data
public class ProcessingResult {

    private String figi;
    private Map<EmaPeriod, EmaData> emaValue = new HashMap<>();
    private List<Double> macdBarValues = new ArrayList<>();
    private List<Double> rsiValues;
    private List<LevelData> levels;
    private double minPrice;
    private double closePrice;
    private double openPrice;
    private double maxPrice;
    private double volume;
    private Trend macdSignalTrend;
    private Trend macdBarTrend;
    private Trend macdDivergence;
    private RsiSign rsiSign;
    private String ticker;
    private double macdDivergenceStatistics;

    @Data
    @Builder
    public static class EmaData {
        private boolean isCloseToEma;
        private boolean isCloseRetest;
        private double emaValue;
        private double difference;
        private LevelType levelType;
    }

    @Data
    @Builder
    public static class LevelData {
        private boolean isClose;
        private boolean isCloseRetest;
        private double value;
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
