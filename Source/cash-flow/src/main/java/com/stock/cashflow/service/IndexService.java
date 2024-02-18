package com.stock.cashflow.service;

import com.stock.cashflow.dto.IndexDTO;
import com.stock.cashflow.dto.IntradayDTO;

public interface IndexService {

    void processIndexHistoricalQuotes(String index, String startDate, String endDate, IndexDTO dto);

    void analyzeIndex(String startDate, String endDate);

    void analyzeIntraday(String tradingDate, IntradayDTO dto);

}
