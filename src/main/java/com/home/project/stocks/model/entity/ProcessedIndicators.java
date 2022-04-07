package com.home.project.stocks.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.home.project.stocks.model.processing.ProcessingResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.math3.util.Precision;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class to store indicator processing data
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "processed_indicator")
public class ProcessedIndicators {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "processedIndicator")
    private List<ProcessedEma> emaData;

    private double rsiValue;
    private String macdSignalTrend;
    private String macdBarTrend;
    private String rsiSign;
    private String ticker;
    private double closePrice;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    public static ProcessedIndicators populateFields(ProcessingResult processingResult,
                                                     LocalDateTime date) {
        var indicator = ProcessedIndicators.builder()
                .closePrice(processingResult.getClosePrice())
                .date(date)
                .ticker(processingResult.getTicker())
                .macdSignalTrend(processingResult.getMacdSignalTrend() != null
                        ? processingResult.getMacdSignalTrend().name() : null)
                .macdBarTrend(processingResult.getMacdBarTrend() != null
                        ? processingResult.getMacdBarTrend().name() : null)
                .rsiSign(processingResult.getRsiSign() != null
                        ? processingResult.getRsiSign().name() : null)
                .build();
        List<ProcessedEma> emaList = Collections.synchronizedList(new ArrayList<>());
        processingResult.getEmaValue().forEach(((emaPeriod, data) ->
                emaList.add(ProcessedEma.builder()
                        .difference(data.getDifference())
                        .emaType(emaPeriod.getPeriod())
                        .levelType(data.getLevelType())
                        .emaValue(Precision.round(data.getEmaValue(), 2))
                        .isCloseToEma(data.isCloseToEma())
                        .processedIndicator(indicator)
                        .datetime(date)
                        .build()))
        );
        indicator.setEmaData(emaList);
        return indicator;

    }

    @Override
    public String toString() {
        var stringBuilder = new StringBuilder();
        emaData.forEach(emaData -> stringBuilder.append("Ема ").append(emaData.getEmaType())
                .append(" на недельном ТФ: ").append(emaData.getEmaValue()).append("\n"));

        return "Тикер: " + ticker + "\n" +
                "Цена закрытия: " + closePrice + "\n" +
                (rsiSign != null ? "Rsi: " + rsiSign + ", " + rsiValue + "\n" : "") +
                (macdBarTrend != null ? "Гистограмма macd: " + macdBarTrend + "\n" : "") +
                (macdSignalTrend != null ? "Пересечение macd: " + macdSignalTrend + "\n" : "") +
                stringBuilder;
    }
}
