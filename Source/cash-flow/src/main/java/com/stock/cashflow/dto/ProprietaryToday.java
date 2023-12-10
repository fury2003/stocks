package com.stock.cashflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ProprietaryToday {
    private String comGroupCode;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date fromDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date toDate;

    private String timeRange;
    private double totalBuyTradeVolume;
    private double totalBuyTradeValue;
    private double totalSellTradeVolume;
    private double totalSellTradeValue;
    private double totalNetBuyTradeVolume;
    private double totalNetBuyTradeValue;
    private double totalNetSellTradeVolume;
    private double totalNetSellTradeValue;
    private List<ProprietaryTrade> buy;
    private List<ProprietaryTrade> sell;
    private List<ProprietaryTrade> netBuy;
    private List<ProprietaryTrade> netSell;
}
