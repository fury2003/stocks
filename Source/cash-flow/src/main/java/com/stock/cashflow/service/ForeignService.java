package com.stock.cashflow.service;

public interface ForeignService {

    void process(String symbol, String startDate, String endDate);

    void processAll(String startDate, String endDate);

    void writeTopBuy(String tradingDate);

    void writeTopSell(String tradingDate);

    void processStatisticTrading(String tradingDate);

    void resetStatistic();

    void resetTopBuySell(String tradingDate);

}
