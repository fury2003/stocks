package com.stock.cashflow.dto;

import lombok.Data;

@Data
public class IncomeSheetItem {

    private String ticker;
    private String periodDateName;
    private String periodDate;

    // Balance Sheet
    private Long is2;
    private Long is4;
    private Long is8;
    private Long is13;
    private Long is14;
    private Long is37;
    private Long is38;
    private Long is39;
    private Long is43;
    private Long is45;
    private Long is48;
    private Long is50;
    private Long is51;
    private Long is52;

}
