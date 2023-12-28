package com.stock.cashflow.service;

public interface CashFlowService {

    void crawlData(String ticker, String period, String size);
}
