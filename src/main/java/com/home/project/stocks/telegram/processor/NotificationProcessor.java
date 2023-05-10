package com.home.project.stocks.telegram.processor;

import org.springframework.data.util.Pair;

import java.util.Collection;

/**
 * @author rlagay
 */
public interface NotificationProcessor {
    Pair<String, Collection<?>> toMessage(Collection<?> indicators);
}
