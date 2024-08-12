package com.stock.cashflow.service;

public interface IntradayOrderService {

    void process(String symbol);
    void processAll();

    void downloadOrderReport(String symbol);

    void analyzeOrder(String tradingDate);

}
