package com.home.project.stocks.telegram.processor;

import com.home.project.stocks.model.entity.ProcessedIndicators;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.home.project.stocks.model.processing.ProcessingResult.Trend.NO_SIGN;

/**
 * @author rlagay
 */
@Component
public class MacdNotificationProcessor implements NotificationProcessor {

    private static final String MSG = "✔️ Акции, с дивергенцией по MACD: \n\n";

    @Override
    public Pair<String, Collection<?>> toMessage(Collection<?> indicators) {
        if (indicators.iterator().hasNext() && indicators.iterator().next() instanceof ProcessedIndicators) {
            return Pair.of(
                MSG, indicators.stream()
                        .map(stocks -> (ProcessedIndicators) stocks)
                        .filter(stocks -> stocks.getMacdDiverTrend() != null && !NO_SIGN.name().equals(stocks.getMacdDiverTrend()))
                        .collect(Collectors.toList()));
        }
        return Pair.of(MSG, Collections.emptyList());
    }
}
