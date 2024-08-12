package com.stock.cashflow.service;

import java.io.FileNotFoundException;

public interface StatisticsService {

    void writeSpecificDate(String symbol, String tradingDate);

    void writeDateToDate(String symbol, String startDate, String endDate);

    void writeAllForSpecificDate(String tradingDate);

    void writeSpecificDataAllSymbolSpecificDate(String tradingDate, String column);

    void writeIndexAnalyzedDateToDate(String startDate, String endDate);

    void highlightOrderBook(String tradingDate);

    void writeProprietaryTopBuySell(String tradingDate, Boolean isLastDayOfWeek);

    void writeForeignTopBuySell(String tradingDate, Boolean isLastDayOfWeek);

    void analyzeOrderBook(String tradingDate);

    void writeOrderBookFromDateToDate(String symbol, String startDate, String endDate);


    void updateIntradayData(String tradingDate, String column);

    void updateMoneyFlowData(String tradingDate, String column);

    void updateBuySellInVn30(String startDate, String endDate, String column) throws FileNotFoundException;

}
