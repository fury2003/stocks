package com.stock.cashflow.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockPriceDataResponse {
    private String code;
    private String message;
    private List<StockPrice> data;

    private Paging paging;

    public static class Paging {
        private int total;
        private int page;
        private int pageSize;
    }
}
