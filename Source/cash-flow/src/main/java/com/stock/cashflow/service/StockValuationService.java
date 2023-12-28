package com.stock.cashflow.service;

public interface StockValuationService {

    void writeDataForSpecificQuarter(String ticker, String quarter);

    void writeDataFromQuarterTo(String ticker, String fromQuarter, String toQuarter);

    void writeDataForSpecificYear(String ticker, String quarter);

}
