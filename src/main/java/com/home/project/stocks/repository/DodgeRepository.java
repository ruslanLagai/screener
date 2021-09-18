package com.home.project.stocks.repository;

import java.util.Date;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.home.project.stocks.model.repositories.DodgeIndex;

/**
 * Dodge repository
 */
public interface DodgeRepository extends ElasticsearchRepository<DodgeIndex, String> {
    DodgeIndex getStocksByFigiAndDateBetween(String figi, Date start, Date end);

    DodgeIndex getStocksByTickerAndDateBetween(String ticker, Date start, Date end);

    DodgeIndex getDodgeIndexByTicker(String ticker);
}
