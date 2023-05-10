package com.home.project.stocks.telegram.processor;

import com.home.project.stocks.model.entity.ProcessedIndicators;
import com.home.project.stocks.model.processing.ProcessingResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author rlagay
 */
public class RsiNotificationProcessor implements NotificationProcessor {

    private static final String MSG = "✔️ Акции, приближающиеся с перепроданностью/перекупленностью: \n\n";

    @Override
    public Pair<String, Collection<?>> toMessage(Collection<?> indicators) {
        if (indicators.iterator().next() instanceof ProcessedIndicators) {
            return Pair.of(
                MSG, indicators.stream()
                        .map(stocks -> (ProcessedIndicators) stocks)
                        .filter(stocks -> StringUtils.isNotBlank(stocks.getRsiSign()))
                        .filter(stocks -> !stocks.getRsiSign().equals(ProcessingResult.RsiSign.NO_SIGN.name()))
                        .collect(Collectors.toList()));
        }
        return Pair.of(MSG, Collections.emptyList());
    }
}
