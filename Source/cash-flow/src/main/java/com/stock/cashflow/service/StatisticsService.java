package com.stock.cashflow.service;

public interface StatisticsService {

    void writeSpecificDate(String symbol, String tradingDate);

    void writeDateToDate(String symbol, String startDate, String endDate);

    void writeAllForSpecificDate(String tradingDate);

    void writeDerivativesDateToDate(String symbol, String startDate, String endDate);

}
