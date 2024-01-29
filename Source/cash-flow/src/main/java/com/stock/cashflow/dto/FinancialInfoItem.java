package com.stock.cashflow.dto;

import lombok.Data;

@Data
public class FinancialInfoItem {
    private String ticker;
    private String periodDateName;
    private String periodDate;

    // Balance Sheet
    private Long is1;
    private Long bs10;
    private Long bs11;
    private Long bs115;
    private Long op49;
}
