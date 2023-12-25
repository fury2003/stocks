package com.stock.cashflow.service;

import com.stock.cashflow.dto.IndexDTO;

public interface IndexService {

    void processIndexHistoricalQuotes(String index, String startDate, String endDate);

}
