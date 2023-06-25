package com.home.project.stocks.indicators.util;

import java.math.BigDecimal;

/**
 * @author rlagay
 */
public class NumberFormatter {
    private NumberFormatter() {
    }

    public static double round(double value) {
        return NumberFormatter.round(value, 2);
    }

    public static double round(double value, int numberOfDigitsAfterDecimalPoint) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(numberOfDigitsAfterDecimalPoint, BigDecimal.ROUND_HALF_UP);
        return bigDecimal.doubleValue();
    }
}
