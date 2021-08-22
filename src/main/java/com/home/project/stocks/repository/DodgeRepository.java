package com.home.project.stocks.repository;


import com.home.project.stocks.model.repositories.DodgeIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.LocalDateTime;

public interface DodgeRepository extends ElasticsearchRepository<DodgeIndex, String> {
     DodgeIndex getStocksByFigiAndCandleDateBetween(String figi, LocalDateTime start, LocalDateTime end);

     DodgeIndex getDodgeIndexByTicker(String ticker);
}
