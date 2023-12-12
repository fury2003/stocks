package com.stock.cashflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DerivativesDTO {

    @JsonProperty("iTotalDisplayRecords")
    private int iTotalDisplayRecords;

    @JsonProperty("aaDataFoot")
    private List<List<String>> aaDataFoot;

    @JsonProperty("iTotalRecords")
    private int iTotalRecords;

    @JsonProperty("aaData")
    private List<List<String>> aaData;

}
