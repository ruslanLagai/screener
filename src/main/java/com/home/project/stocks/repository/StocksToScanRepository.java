package com.home.project.stocks.repository;

import com.home.project.stocks.model.repositories.StocksToScanIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface StocksToScanRepository extends ElasticsearchRepository<StocksToScanIndex, String> {
    StocksToScanIndex findCandleIndexByTicker(String ticker);
}
