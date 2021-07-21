package com.home.project.stocks.parser;

import com.home.project.stocks.model.aplha.vantage.Ema;
import com.home.project.stocks.model.indicators.ParsedEma;
import lombok.extern.log4j.Log4j2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class AlphaVantageEmaParser {

    public static ParsedEma parseVantageEma(Ema vantageEma) {
        Map<Date, Double> data = new HashMap<>();
        vantageEma.getDates().forEach((k, v) -> {
            SimpleDateFormat formatter = k.contains(":") ?
                    new SimpleDateFormat("dd-M-yyyy hh:mm") :
                    new SimpleDateFormat("dd-M-yyyy");
            try {
                Date date = formatter.parse(k);
                var value = Double.valueOf(v.getEma());
                data.put(date, value);
            } catch (ParseException e) {
                log.error("Failed to parse date in response. " + e.getMessage());
            }
        });
        return new ParsedEma(data);
    }
}
