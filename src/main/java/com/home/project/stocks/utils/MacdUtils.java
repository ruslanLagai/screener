package com.home.project.stocks.utils;

import com.home.project.stocks.model.processing.MacdData;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static java.lang.Math.abs;

/**
 * @author rlagay
 */
@Slf4j
public class MacdUtils {

    public static MacdData getExtremum(List<MacdData> prevHill) {
        var extremumValue = prevHill.stream()
            .map(MacdData::getMacdBarValue)
            .mapToDouble(Double::doubleValue)
            .map(Math::abs)
            .max()
            .orElseGet(() -> {
                log.debug("Failed to find extremum value");
                return 0.0;
            });

        return prevHill.stream()
            .filter(macdData -> abs(macdData.getMacdBarValue()) == extremumValue)
            .findFirst()
            .orElseGet(() -> {
                log.debug("Failed to find extremum for latest hill");
                return null;
            });
    }


    public static boolean checkAscDivergence(MacdData latest, MacdData prev) {
        return prev.getMacdBarValue() < latest.getMacdBarValue()
            && prev.getClosePrice() > latest.getClosePrice()
            && prev.getClosePrice() / latest.getClosePrice() > 1.03;
    }

    public static boolean checkDescDivergence(MacdData latest, MacdData prev) {
        return abs(prev.getMacdBarValue()) > abs(latest.getMacdBarValue())
            && prev.getClosePrice() < latest.getClosePrice()
            && (latest.getClosePrice() / prev.getClosePrice()) > 1.03;
    }

}
