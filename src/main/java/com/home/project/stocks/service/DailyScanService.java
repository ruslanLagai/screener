package com.home.project.stocks.service;

/**
 * @author rlagay
 */
public interface DailyScanService {
    void processStock(String ticker, String figi);

}
