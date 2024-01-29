package com.stock.cashflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FinancialInfoSheetResponse {

    private int status;
    private String message;

    @JsonProperty("data")
    private FinancialInfoItems data;

}
