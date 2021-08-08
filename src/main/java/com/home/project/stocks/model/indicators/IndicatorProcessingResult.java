package com.home.project.stocks.model.indicators;

import com.home.project.stocks.model.aplha.vantage.EmaPeriod;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class IndicatorProcessingResult {
    private Map<EmaPeriod, EmaData> emaValue;
    private List<Double> momentumValues;
    private List<Double> rsiValues;
    private double minPrice;
    private double closePrice;
    private double openPrice;
    private double maxPrice;
    private Trend macdSignalTrend;
    private Trend macdBarTrend;
    private RsiSign rsiSign;

    public IndicatorProcessingResult() {
        emaValue = new HashMap<>();
        momentumValues = new ArrayList<>();
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
