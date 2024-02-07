package com.stock.cashflow.dto;

import lombok.Data;

@Data
public class FinancialInfoItem {
    private String ticker;
    private String periodDateName;
    private String periodDate;

    // Balance Sheet
    private Long is1;
    private Long is9;
    private Long is14;

    private Long bs10;
    private Long bs11;
    private Long op49;
}
