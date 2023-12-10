package com.stock.cashflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ProprietaryTrade {
    private String organCode;
    private String ticker;
    private String timeRange;
    private String marketType;
    private double priceChange;
    private double percentPriceChange;
    private double totalBuyTradeVolume;
    private double totalBuyTradeValue;
    private double totalSellTradeVolume;
    private double totalSellTradeValue;
    private double totalNetBuyTradeValue;
    private double totalNetSellTradeValue;
    private double totalNetBuyTradeVolume;
    private double totalNetSellTradeVolume;
    private double ceilingPrice;
    private double floorPrice;
    private double matchPrice;
    private double referencePrice;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date fromDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date toDate;
}
