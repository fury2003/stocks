package com.stock.cashflow.dto;

import lombok.Data;

@Data
public class BalanceSheetItem {

    private String ticker;
    private String periodDateName;
    private String periodDate;

    // Balance Sheet
    private Long bs1;
    private Long bs2;
    private Long bs3;
    private Long bs6;
    private Long bs8;
    private Long bs9;
    private Long bs10;
    private Long bs13;
    private Long bs16;
    private Long bs20;
    private Long bs29;

}
