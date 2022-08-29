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
import java.util.Collections;
import java.util.List;

import static com.home.project.stocks.model.processing.ProcessingResult.Trend.NO_SIGN;

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
    private String macdDiverTrend;
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
                .macdDiverTrend(processingResult.getMacdDivergence() != null
                        ? processingResult.getMacdDivergence().name() : null)
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
                        .isCloseRetest(data.isCloseRetest())
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
        emaData.stream().filter(data -> data.isCloseToEma() && !data.isCloseRetest())
                .forEach(emaData -> stringBuilder.append("Ема ").append(emaData.getEmaType())
                        .append(" на недельном ТФ: ").append(emaData.getEmaValue()).append("\n"));

        var macdBarIcon = ProcessingResult.Trend.ASCENDING.name().equals(macdBarTrend) ? "↗️" : "↘️";
        var macdSignalIcon = ProcessingResult.Trend.ASCENDING.name().equals(macdSignalTrend) ? "↗️" : "↘️";
        var macdDiverIcon = ProcessingResult.Trend.ASCENDING.name().equals(macdDiverTrend) ? "↗️" : "↘️";

        return "Тикер: " + ticker + "\n" +
                "Цена закрытия: " + closePrice + "\n" +
                (rsiSign != null ? "Rsi: " + rsiSign + ", " + rsiValue + "\n" : "") +
                (macdBarTrend != null && !macdBarTrend.equals(NO_SIGN.name()) ? "Гистограмма macd: " + macdBarIcon + "\n" : "") +
                (macdSignalTrend != null && !macdSignalTrend.equals(NO_SIGN.name()) ? "Пересечение macd: " + macdSignalIcon + "\n" : "") +
                (macdDiverTrend != null && !macdDiverTrend.equals(NO_SIGN.name()) ? "Дивергенция по macd: " + macdDiverIcon + "\n" : "") +
                stringBuilder;
    }
}
