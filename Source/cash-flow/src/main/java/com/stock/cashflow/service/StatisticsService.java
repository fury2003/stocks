package com.stock.cashflow.service;

public interface StatisticsService {

    void writeSpecificDate(String symbol, String tradingDate);

    void writeDateToDate(String symbol, String startDate, String endDate);

    void writeAllForSpecificDate(String tradingDate);

    void writeDerivativesDateToDate(String symbol, String startDate, String endDate);

    void writeSpecificDataAllSymbolSpecificDate(String tradingDate, String column);

    void writeSpecificDataSpecificSymbolFromTo(String symbol, String startDate, String endDate, String column);

    void writeIndexAnalyzedDateToDate(String startDate, String endDate);

    void writePriceChangeMonthly(String sheetName, String startDate, String endDate);

}
