package com.stock.cashflow.dto;

import lombok.Data;

import java.util.List;

@Data
public class IncomeSheetItems {

    private String industryGroup;

    private List<IncomeSheetItem> items;
}
