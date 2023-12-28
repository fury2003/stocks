package com.stock.cashflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IncomeSheetResponse {

    private int status;
    private String message;

    @JsonProperty("data")
    private IncomeSheetItems data;

}
