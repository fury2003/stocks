package com.stock.cashflow.dto;

import lombok.Data;

import java.util.List;

@Data
public class DerivativesForeignItems {

    private int totalCount;
    private List<DerivativesForeignItem> items;

}
