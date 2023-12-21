package com.stock.cashflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DerivativesMarketItems {

    @JsonProperty("totalCount")
    private int totalCount;

    @JsonProperty("items")
    private List<DerivativesMarket> items;

}
