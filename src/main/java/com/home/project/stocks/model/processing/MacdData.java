package com.home.project.stocks.model.processing;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Class to collect data for macd divergence processing
 */
@Data
@Builder
public class MacdData {

    private double closePrice;
    private double macdBarValue;
    private double macdSignalValue;
    private double macdValue;
    private LocalDateTime dateTime;
    private String ticker;
}
