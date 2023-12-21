package com.stock.cashflow.service;


import com.stock.cashflow.dto.DerivativesProprietaryDTO;

public interface DerivativesService {

    void process(String symbol, String startDate, String endDate);

    void updateProprietary(String symbol, String tradingDate, DerivativesProprietaryDTO derivativesProprietaryDTO);

}
