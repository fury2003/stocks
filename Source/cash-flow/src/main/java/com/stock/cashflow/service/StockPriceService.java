package com.stock.cashflow.service;

import java.text.ParseException;

public interface StockPriceService {

    void process(String symbol, String startDate, String endDate) throws ParseException;

    void processAllSSI(String startDate, String endDate) throws ParseException;

    void processAllFireant(String startDate, String endDate) throws ParseException;
}
