package com.stock.cashflow.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProprietaryDataResponse {
    private int page;
    private int pageSize;
    private int totalCount;
    private List<ProprietaryItems> items;
    private String packageId;
    private String status;
    private List<String> errors;

}
