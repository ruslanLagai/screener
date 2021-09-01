package com.home.project.stocks.repository;


import com.home.project.stocks.model.repositories.HammerIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.LocalDateTime;

public interface HammerRepository extends ElasticsearchRepository<HammerIndex, String> {
     HammerIndex getStocksByTickerAndDateBetween(String figi, LocalDateTime start, LocalDateTime end);
     HammerIndex getHammerIndexByTicker(String ticker);
}
