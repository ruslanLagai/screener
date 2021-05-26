package com.home.project.stocks.model.repositories;

import com.home.project.stocks.model.candles.Candle;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Builder
@Document(indexName = "candle_index")
public class CandleIndex {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String figi;

    @Field(type = FieldType.Text)
    private String interval;

    @Field(type = FieldType.Double)
    private double o;

    @Field(type = FieldType.Double)
    private double c;

    @Field(type = FieldType.Double)
    private double h;

    @Field(type = FieldType.Double)
    private double l;

    @Field(type = FieldType.Double)
    private double v;

    @Field(type = FieldType.Date)
    private LocalDateTime time;

    public static CandleIndex populateFields(Candle candle) {
        return CandleIndex.builder()
                .o(candle.getO())
                .c(candle.getC())
                .h(candle.getH())
                .l(candle.getL())
                .v(candle.getV())
                .figi(candle.getFigi())
                .interval(candle.getInterval())
                .time(candle.getTime())
                .build();

    }
}
