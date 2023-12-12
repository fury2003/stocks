package com.stock.cashflow.service;


public interface DerivativesService {

    void process(String symbol, String startDate, String endDate, String token);

}
