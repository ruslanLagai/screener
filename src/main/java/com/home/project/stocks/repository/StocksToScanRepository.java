package com.home.project.stocks.repository;

import com.home.project.stocks.model.entity.StocksToScan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StocksToScanRepository extends JpaRepository<StocksToScan, String> {
    StocksToScan findCandleIndexByTicker(String ticker);
}
