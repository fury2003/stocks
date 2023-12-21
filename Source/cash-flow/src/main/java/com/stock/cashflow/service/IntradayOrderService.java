package com.stock.cashflow.service;

public interface IntradayOrderService {

    void process(String symbol);

    void processAll();
}
