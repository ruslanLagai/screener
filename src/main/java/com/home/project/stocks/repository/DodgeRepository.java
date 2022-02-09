package com.home.project.stocks.repository;

import java.time.LocalDateTime;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.home.project.stocks.model.repositories.DodgeIndex;

/**
 * Dodge repository
 */
public interface DodgeRepository extends ElasticsearchRepository<DodgeIndex, String> {
    DodgeIndex getStocksByFigiAndDateBetween(String figi, LocalDateTime start, LocalDateTime end);

    DodgeIndex getStocksByTickerAndDateBetween(String ticker, LocalDateTime start, LocalDateTime end);

    DodgeIndex getDodgeIndexByTicker(String ticker);
}
