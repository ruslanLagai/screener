package com.home.project.stocks.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Class to store indicator processing data
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "daily_indicator")
@NamedEntityGraph(
        name = "DailyIndicator.fetch-all-data-entity-graph",
        attributeNodes = {
                @NamedAttributeNode("emaData"),
                @NamedAttributeNode("rsiData"),
                @NamedAttributeNode("momData"),
                @NamedAttributeNode("macdData")
        }
)
public class DailyIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "dailyIndicator")
    private Set<DailyEma> emaData;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "dailyIndicator")
    private Set<DailyRsi> rsiData;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "dailyIndicator")
    private Set<DailyMom> momData;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "dailyIndicator")
    private Set<DailyMacd> macdData;
    private String timeframe;
    private String ticker;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyIndicator that = (DailyIndicator) o;
        return timeframe.equals(that.timeframe) && ticker.equals(that.ticker) && date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeframe, ticker, date);
    }
}
