package com.stock.cashflow.service;

public interface StockService {

    void processStockPrice(String symbol, String startDate, String endDate);

    void processForeign(String symbol, String startDate, String endDate, String token);

    void processIntraday(String symbol, String date);

}
