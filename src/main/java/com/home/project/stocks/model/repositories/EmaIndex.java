package com.home.project.stocks.model.repositories;

import com.home.project.stocks.model.processing.ProcessingResult;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * Class to store ema data
 */
@Data
@Builder
public class EmaIndex {

    @Field(type = FieldType.Boolean)
    private boolean isCloseToEma;

    @Field(type = FieldType.Double)
    private double emaValue;

    @Field(type = FieldType.Double)
    private double difference;

    @Field(type = FieldType.Keyword)
    private String levelType;

    @Field(type = FieldType.Text)
    private String emaType;

    public static EmaIndex populateFields(ProcessingResult.EmaData emaData, String emaType) {
        return EmaIndex.builder()
                .difference(emaData.getDifference())
                .emaValue(emaData.getEmaValue())
                .isCloseToEma(emaData.isCloseToEma())
                .levelType(emaData.getLevelType().name())
                .emaType(emaType)
                .build();

    }
}
