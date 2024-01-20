package com.stock.cashflow.dto.ssi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LatestPriceItem {
    @JsonProperty("priceInfo")
    private LatestPriceDTO priceInfo;

    @JsonProperty("bidAskInfo")
    private BidAskInfoDTO bidAskInfo;

}
