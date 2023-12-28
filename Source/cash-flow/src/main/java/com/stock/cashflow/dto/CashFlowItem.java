package com.stock.cashflow.dto;

import lombok.Data;

@Data
public class CashFlowItem {

    private String ticker;
    private String periodDateName;
    private String periodDate;

    // Balance Sheet
    private Long cf1;
    private Long cf22;
    private Long cf30;
    private Long cf37;
    private Long cf38;
    private Long cf40;


}
