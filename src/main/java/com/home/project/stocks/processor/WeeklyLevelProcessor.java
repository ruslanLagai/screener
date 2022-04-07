package com.home.project.stocks.processor;

import com.home.project.stocks.model.candles.Candle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Processor to detect weekly levels
 *
 * @author rlagay
 */
@Component
@Slf4j
public class WeeklyLevelProcessor implements LevelProcessor {

    private static final int PERIOD_TO_DETECT_LEVEL = 10;

    @Override
    public Set<Double> processStock(String ticker, List<Candle> candles) {
        log.debug("Starting weekly levels processing for {}", ticker);

        Set<Double> levels = Collections.synchronizedSet(new HashSet<>());
        levels.addAll(detectSupportLevels(candles));
        levels.addAll(detectResistanceLevels(candles));

        return levels;
    }

    /**
     * 1. candles are ordered
     * 2. get first 10 candles, detect min
     * 3. check that following 10 values are >= min && prev 10 values are >= min
     *      a. it is level -> go to step 2, starting from min_index + 10
     *      b. not level -> go to step 2, starting from min_index
     *
     * @param candles   - list of candles ordered by date
     * @return          - List of support levels
     */
    private Set<Double> detectSupportLevels(List<Candle> candles) {
        Set<Double> levels = Collections.synchronizedSet(new HashSet<>());
        var startIndex = PERIOD_TO_DETECT_LEVEL;
        while (startIndex < candles.size() - PERIOD_TO_DETECT_LEVEL) {
            var sublist = candles.subList(startIndex, startIndex + PERIOD_TO_DETECT_LEVEL);
            var minCandle = sublist.stream().min(Comparator.comparing(Candle::getL)).orElse(new Candle());
            var followingSublist = getFollowingSublist(candles, candles.indexOf(minCandle));
            var previousSublist = getPreviousSublist(candles, candles.indexOf(minCandle));
            if (isCandlesLarger(followingSublist, minCandle.getL())
                    && isCandlesLarger(previousSublist, minCandle.getL())) {
                levels.add(minCandle.getL());
                startIndex = candles.indexOf(minCandle) + PERIOD_TO_DETECT_LEVEL;

                log.trace("Detected level {}", minCandle.getL());
            } else {
                startIndex++;
            }

        }
        log.debug("Detected {} levels", levels.size());
        return levels;
    }

    /**
     * 1. candles are ordered
     * 2. get first 10 candles, detect max
     * 3. check that following 10 values are <= max && prev 10 values are <= max
     *      a. it is level -> go to step 2, starting from max_index + 10
     *      b. not level -> go to step 2, starting from max_index
     *
     * @param candles   - list of candles ordered by date
     * @return          - List of resistance levels
     */
    private Set<Double> detectResistanceLevels(List<Candle> candles) {
        Set<Double> levels = Collections.synchronizedSet(new HashSet<>());
        var startIndex = 0;
        while (startIndex < candles.size() - PERIOD_TO_DETECT_LEVEL) {
            var sublist = candles.subList(startIndex, startIndex + PERIOD_TO_DETECT_LEVEL);
            var maxCandle = sublist.stream().max(Comparator.comparing(Candle::getH)).orElse(new Candle());
            var followingSublist = getFollowingSublist(candles, candles.indexOf(maxCandle));
            var previousSublist = getPreviousSublist(candles, candles.indexOf(maxCandle));
            if (isCandlesLess(followingSublist, maxCandle.getH())
                    && isCandlesLess(previousSublist, maxCandle.getH())) {
                levels.add(maxCandle.getH());
                startIndex = candles.indexOf(maxCandle) + PERIOD_TO_DETECT_LEVEL;

                log.trace("Detected level {}", maxCandle.getH());
            } else {
                startIndex++;
            }

        }
        log.debug("Detected {} levels", levels.size());
        return levels;
    }

    private List<Candle> getFollowingSublist(List<Candle> candles, int index) {
        return index + PERIOD_TO_DETECT_LEVEL <= candles.size()
                ? candles.subList(index + 1, index + PERIOD_TO_DETECT_LEVEL) : null;
    }

    private List<Candle> getPreviousSublist(List<Candle> candles, int index) {
        return index - PERIOD_TO_DETECT_LEVEL >= 0
                ? candles.subList(index - PERIOD_TO_DETECT_LEVEL, index) : null;
    }

    /**
     * Check if prev/next candles are larger
     * @param list          - candles to check
     * @param minimum       - min val
     * @return              - tru/false
     */
    private boolean isCandlesLarger(List<Candle> list, Double minimum) {
        return list != null &&
                minimum < list.stream().map(Candle::getL).mapToDouble(Double::doubleValue).min().orElse(0.0);
    }

    private boolean isCandlesLess(List<Candle> list, Double max) {
        return list != null &&
                max > list.stream().map(Candle::getH).mapToDouble(Double::doubleValue).max().orElse(Integer.MAX_VALUE);
    }
}
