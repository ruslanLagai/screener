package com.home.project.stocks.model.repositories;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * List of stocks to be scanned
 */
@Data
@Builder
@Document(indexName = "stocks_to_scan_index")
public class StocksToScanIndex {
    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String ticker;

    @Field(type = FieldType.Keyword)
    private String figi;

    @Field(type = FieldType.Text)
    private String name;

    public static StocksToScanIndex populateFields(String figi, String ticker, String name) {
        return StocksToScanIndex.builder()
                .figi(figi)
                .ticker(ticker)
                .name(name)
                .build();
    }
}
