package com.home.project.stocks.repository;

import com.home.project.stocks.model.repositories.CandleIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CandleRepository extends ElasticsearchRepository<CandleIndex, String> {
    CandleIndex findCandleIndexByFigi(String figi);
}
