package com.stock.cashflow.dto.ssi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LatestPriceDTO {

    private String ticker;
    private String tradingDate;
    private String openPrice;
    private String highestPrice;
    private String lowestPrice;
    private String closePrice;
    private String priceChange;
    private String percentPriceChange;
    private String totalMatchVolume;


}
