package com.stock.cashflow.service;

public interface IndexService {

    public void process(String index, String date, String token);

    void processProprietaryTradingValue(String index, String quarter);

}
