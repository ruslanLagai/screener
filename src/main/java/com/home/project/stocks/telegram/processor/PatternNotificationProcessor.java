package com.home.project.stocks.telegram.processor;

import com.home.project.stocks.model.entity.Candle;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

/**
 * @author rlagay
 */
@Component
public class PatternNotificationProcessor implements NotificationProcessor {

    private static final String MSG = "✔️ Акции с паттернами: \n\n";

    @Override
    public Pair<String, Collection<?>> toMessage(Collection<?> indicators) {
        if (indicators.iterator().hasNext() && indicators.iterator().next() instanceof Candle) {
            return Pair.of(MSG, indicators);
        }
        return Pair.of(MSG, Collections.emptyList());
    }
}
