package com.stock.cashflow.persistence.dto;

import lombok.Data;

@Data
public class SymbolTotalNetValueDTO {
    private String symbol;
    private Double totalNetValueSum;

    public SymbolTotalNetValueDTO(String symbol, Double totalNetValueSum) {
        this.symbol = symbol;
        this.totalNetValueSum = totalNetValueSum;
    }
}
