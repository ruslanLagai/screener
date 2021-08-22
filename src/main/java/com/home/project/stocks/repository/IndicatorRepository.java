package com.home.project.stocks.repository;

import com.home.project.stocks.model.repositories.IndicatorsIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface IndicatorRepository extends ElasticsearchRepository<IndicatorsIndex, String> {
    IndicatorsIndex getByTicker(String ticker);
}
