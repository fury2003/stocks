package com.stock.cashflow.service;

public interface StockValuationService {

    void writeDataForSpecificQuarter(String ticker, String quarter);

    void writeDataFromQuarterTo(String ticker, String fromQuarter, String toQuarter);

    void writeDataForSpecificYear(String ticker, String quarter);

    void writeDataFromToForSpecificColumn(String ticker, String fromQuarter, String toQuarter, int column);

    void updateLatestPrice(String tradingDate);

    void getGeneralInfo(String ticker, String year);

    void getGeneralInfoForAll(String year);

}
