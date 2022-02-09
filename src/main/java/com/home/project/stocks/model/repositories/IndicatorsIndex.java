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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to store indicator processing data
 */
@Data
@Builder
@Document(indexName = "indicators_index")
public class IndicatorsIndex {

    @Id
    private String id;

    @Field(type = FieldType.Nested)
    private List<EmaIndex> emaData;

    @Field(type = FieldType.Double)
    private List<Double> rsiValue;

    @Field(type = FieldType.Double)
    private List<Double> macdBarValue;

    @Field(type = FieldType.Keyword)
    private String macdSignalTrend;

    @Field(type = FieldType.Keyword)
    private String macdBarTrend;

    @Field(type = FieldType.Keyword)
    private String rsiSign;

    @Field(type = FieldType.Text)
    private String ticker;

    @Field(type = FieldType.Keyword)
    private String candleId;

    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "uuuu-MM-dd'T'HH:mm:ss.Z")
    private LocalDateTime date;

    public static IndicatorsIndex populateFields(ProcessingResult processingResult, String candleId,
                                                 LocalDateTime date) {
        List<EmaIndex> emaIndices = new ArrayList<>();
        if (processingResult.getEmaValue() != null) {
            processingResult.getEmaValue().forEach((k, v) ->
                    emaIndices.add(EmaIndex.populateFields(v, k.getPeriod())));
        }
        return IndicatorsIndex.builder()
                .candleId(candleId)
                .date(date)
                .macdBarValue(processingResult.getMacdBarValues())
                .rsiValue(processingResult.getRsiValues())
                .ticker(processingResult.getTicker())
                .macdSignalTrend(processingResult.getMacdSignalTrend().name())
                .macdBarTrend(processingResult.getMacdBarTrend().name())
                .rsiSign(processingResult.getRsiSign().name())
                .emaData(emaIndices)
                .build();

    }
}
