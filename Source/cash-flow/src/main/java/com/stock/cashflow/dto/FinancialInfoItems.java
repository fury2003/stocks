package com.stock.cashflow.dto;

import lombok.Data;

import java.util.List;

@Data
public class FinancialInfoItems {

    private String industryGroup;

    private List<FinancialInfoItem> items;


}
