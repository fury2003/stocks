package com.stock.cashflow.service;

import com.stock.cashflow.dto.ProprietaryDataResponse;

public interface ProprietaryService {

    void processFireant();

    void processAllFloorsFromSSI();

    void processSSI(ProprietaryDataResponse proprietaryTextData);

    void processStatisticTrading(String tradingDate);

    void writeTopBuy(String tradingDate);

    void writeTopSell(String tradingDate);

    void resetTopBuySell(String tradingDate);

    void resetStatistic();

}
