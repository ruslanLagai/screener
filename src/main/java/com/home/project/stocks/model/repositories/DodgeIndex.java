package com.home.project.stocks.model.repositories;

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
@Document(indexName = "dodge_index")
public class DodgeIndex {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String figi;

    @Field(type = FieldType.Text)
    private String ticker;

    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    @Field(type = FieldType.Text)
    private String candleId;
}
