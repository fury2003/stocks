package com.stock.cashflow.dto.ssi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class LatestPriceResponse {

    @JsonProperty("page")
    private int page;

    @JsonProperty("pageSize")
    private int pageSize;

    @JsonProperty("totalCount")
    private int totalCount;

    @JsonProperty("items")
    private List<LatestPriceItem> items;

    @JsonProperty("packageId")
    private String packageId;

    @JsonProperty("status")
    private String status;

}
