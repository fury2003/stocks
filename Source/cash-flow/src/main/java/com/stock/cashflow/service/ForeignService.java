package com.stock.cashflow.service;

public interface ForeignService {

    void process(String symbol, String startDate, String endDate);

    void processAll(String startDate, String endDate);

}
