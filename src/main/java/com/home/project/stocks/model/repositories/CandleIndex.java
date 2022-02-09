package com.home.project.stocks.model.repositories;

import com.home.project.stocks.model.candles.Candle;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@Document(indexName = "candle_index")
public class CandleIndex {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String ticker;

    @Field(type = FieldType.Text)
    private String interval;

    @Field(type = FieldType.Double)
    private double open;

    @Field(type = FieldType.Double)
    private double close;

    @Field(type = FieldType.Double)
    private double high;

    @Field(type = FieldType.Double)
    private double low;

    @Field(type = FieldType.Double)
    private double volume;

    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "uuuu-MM-dd'T'HH:mm:ss.Z")
    private LocalDateTime time;

    public static CandleIndex populateFields(Candle candle, String ticker) {
        return CandleIndex.builder()
                .open(candle.getO())
                .close(candle.getC())
                .high(candle.getH())
                .low(candle.getL())
                .volume(candle.getV())
                .time(candle.getTime())
                .ticker(ticker)
                .build();
    }
}
