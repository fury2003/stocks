package com.stock.cashflow.dto;

import lombok.Data;

@Data
public class StockPrice {
    private String tradingDate;
    private String priceChange;
    private String perPriceChange;
    private String ceilingPrice;
    private String floorPrice;
    private String refPrice;
    private String openPrice;
    private String highestPrice;
    private String lowestPrice;
    private String closePrice;
    private String averagePrice;
    private String closePriceAdjusted;
    private String totalMatchVol;
    private String totalMatchVal;
    private String totalDealVal;
    private String totalDealVol;
    private String foreignBuyVolTotal;
    private String foreignCurrentRoom;
    private String foreignSellVolTotal;
    private String foreignBuyValTotal;
    private String foreignSellValTotal;
    private String totalBuyTrade;
    private String totalBuyTradeVol;
    private String totalSellTrade;
    private String totalSellTradeVol;
    private String netBuySellVol;
    private String netBuySellVal;
    private String exchange;
    private String symbol;
    private String foreignBuyVolMatched;
    private String foreignBuyVolDeal;
}
