package com.home.project.stocks.indicators;

import com.home.project.stocks.indicators.util.NumberFormatter;

import java.util.Arrays;

/**
 * @author rlagay
 */
public class ExponentialMovingAverage {
    private double[] prices;

    private double[] periodSma;
    private double smoothingConstant;
    private double[] periodEma;

    public ExponentialMovingAverage calculate(double[] prices, int period) throws Exception {
        ExponentialMovingAverage s;
        if (period >= prices.length)
            throw new Exception("Given period is bigger then given set of prices");

        this.prices = prices;

        this.smoothingConstant = 2d / (period + 1);

        this.periodSma = new double[this.prices.length];
        this.periodEma = new double[this.prices.length];

        SimpleMovingAverage sma = new SimpleMovingAverage();

        for (int i = (period - 1); i < this.prices.length; i++) {
            double[] slice = Arrays.copyOfRange(this.prices, 0, i + 1);
            double[] smaResults = sma.calculate(slice, period).getSMA();
            this.periodSma[i] = smaResults[smaResults.length - 1];

            if (i == (period - 1)) {
                this.periodEma[i] = this.periodSma[i];
            } else if (i > (period - 1)) {
                // Formula: (Close - EMA(previous day)) x multiplier +
                // EMA(previous day)
                this.periodEma[i] = (this.prices[i] - periodEma[i - 1]) * this.smoothingConstant
                    + this.periodEma[i - 1];
            }

            this.periodEma[i] = NumberFormatter.round(this.periodEma[i]);
        }

        return this;
    }

    public double[] getEMA() {
        return this.periodEma;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.prices.length; i++) {
            sb.append(String.format("%02.2f", this.prices[i]));
            sb.append(" ");
            sb.append(String.format("%02.2f", this.periodSma[i]));
            sb.append(" ");
            sb.append(String.format("%02.2f", this.smoothingConstant));
            sb.append(" ");
            sb.append(String.format("%02.2f", this.periodEma[i]));
            sb.append("\n");
        }

        return sb.toString();
    }
}
