package com.stock.cashflow.dto;

import lombok.Data;

import java.util.List;

@Data
public class IntradayData {
    private List<Intraday> data;
    private int currentPage;
    private int size;
    private int totalElements;
    private int totalPages;
}
