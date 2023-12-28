package com.stock.cashflow.dto;

import lombok.Data;

import java.util.List;

@Data
public class CashFlowItems {
    private String industryGroup;

    private List<CashFlowItem> items;

}
