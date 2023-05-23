package com.home.project.stocks.telegram.processor;

import com.home.project.stocks.model.entity.ProcessedLevels;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

/**
 * @author rlagay
 */
@Component
public class LevelsNotificationProcessor implements NotificationProcessor {

    private static final String MSG = "✔️ Акции, приближающиеся к недельным уровням: \n\n";

    @Override
    public Pair<String, Collection<?>> toMessage(Collection<?> levels) {
        if (levels.iterator().hasNext() && levels.iterator().next() instanceof ProcessedLevels) {
            return Pair.of(MSG, levels);
        }
        return Pair.of(MSG, Collections.emptyList());
    }
}
