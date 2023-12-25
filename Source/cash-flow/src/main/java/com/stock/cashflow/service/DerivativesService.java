package com.stock.cashflow.service;


import com.stock.cashflow.dto.DerivativesDTO;
import com.stock.cashflow.dto.DerivativesProprietaryDTO;

import java.util.List;

public interface DerivativesService {

    void processMarketData(List<DerivativesDTO> dto);

    void processForeignData(String symbol, String startDate, String endDate);
    void updateProprietary(String symbol, String tradingDate, DerivativesProprietaryDTO derivativesProprietaryDTO);

}
