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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

/**
 * Class to store ema data
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "processed_ema")
public class ProcessedEma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "processed_indicator_id", nullable = false)
    private ProcessedIndicators processedIndicator;

    private double emaValue;
    private String emaType;
    private boolean isCloseToEma;
    private double difference;
    @Enumerated(EnumType.STRING)
    private ProcessingResult.LevelType levelType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime datetime;
}
