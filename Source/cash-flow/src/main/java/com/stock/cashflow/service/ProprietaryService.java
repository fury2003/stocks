package com.stock.cashflow.service;

import com.stock.cashflow.dto.ProprietaryDataResponse;

public interface ProprietaryService {

    void processFireant();

    void processAllFloorsFromSSI();

    void processSSI(ProprietaryDataResponse proprietaryTextData);

    void processVolatileTrading(String tradingDate);

    void processStatisticTrading(String tradingDate);

}
