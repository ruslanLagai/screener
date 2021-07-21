package com.home.project.stocks.model.indicators;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class ParsedEma {
    private Map<Date, Double> ema;
}
