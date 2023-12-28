package com.stock.cashflow.dto;

import lombok.Data;

import java.util.List;

@Data
public class BalanceSheetItems {
    private String industryGroup;

    private List<BalanceSheetItem> items;

}
