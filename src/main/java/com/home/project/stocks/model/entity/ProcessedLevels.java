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
import java.time.LocalDateTime;
/**
 * Class to store indicator processing data
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "processed_level")
public class ProcessedLevels {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private double level;
    private String ticker;
    private double closePrice;
    private double successRate;
    private double averageBreaking;
    private double averageRebound;
    private int totalCrosses;
    private int goodSignals;
    @Enumerated(EnumType.STRING)
    private ProcessingResult.LevelType levelType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    @Override
    public String toString() {
        var type = levelType.equals(ProcessingResult.LevelType.SUPPORT) ? "поддержки" : "сопротивления";
        return "Тикер: " + ticker + "\n" +
                "Цена закрытия: " + closePrice + "\n" +
                "Ближайший уровень " + type + ": " + level + "\n" +
                "Уровень отработал " + goodSignals + " из " + totalCrosses + "за последние 500 дней." + "\n" +
                "Среднее пробитие: " + averageBreaking + "\n" +
                "Среднее отскок: " + averageRebound;
    }
}
