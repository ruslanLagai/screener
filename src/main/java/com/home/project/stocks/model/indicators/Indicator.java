package com.home.project.stocks.model.indicators;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.home.project.stocks.model.api.EmaPeriod;
import com.home.project.stocks.utils.DateTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author rlagay
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Indicator {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = DateTimeDeserializer.class)
    private LocalDateTime datetime;
    private double rsi;
    private EmaPeriod emaPeriod;
    private double ema;
    private double mom;
    private double macd;
    @JsonProperty("macd_signal")
    private double macdSignal;
    @JsonProperty("macd_hist")
    private double macdHist;
}
