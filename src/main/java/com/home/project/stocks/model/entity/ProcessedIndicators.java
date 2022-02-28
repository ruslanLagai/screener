package com.home.project.stocks.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.home.project.stocks.model.processing.ProcessingResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Class to store indicator processing data
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "processed_indicators")
public class ProcessedIndicators {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToMany
    private List<DailyEma> emaData;

    private double rsiValue;
    private String macdSignalTrend;
    private String macdBarTrend;
    private String rsiSign;
    private String ticker;
    @Column(unique = true)
    private long candleId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    public static ProcessedIndicators populateFields(ProcessingResult processingResult, long candleId,
                                                     LocalDateTime date) {
        return ProcessedIndicators.builder()
                .candleId(candleId)
                .date(date)
                .ticker(processingResult.getTicker())
                .macdSignalTrend(processingResult.getMacdSignalTrend().name())
                .macdBarTrend(processingResult.getMacdBarTrend().name())
                .rsiSign(processingResult.getRsiSign().name())
                .build();

    }
}
