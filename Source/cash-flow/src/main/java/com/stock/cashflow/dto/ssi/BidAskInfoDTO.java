package com.stock.cashflow.dto.ssi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BidAskInfoDTO {
    @JsonProperty("organCode")
    private String organCode;

    @JsonProperty("comGroupCode")
    private String comGroupCode;

    @JsonProperty("tradingDate")
    private String tradingDate;
}
