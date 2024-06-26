package com.stock.cashflow.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TradingStatistics {

    private String tradingDate;

    private Double foreignBuyVolume;
    private Double foreignSellVolume;
    private Double foreignBuyValue;
    private Double foreignSellValue;

    private Double proprietaryBuyVolume;
    private Double proprietarySellVolume;
    private Double proprietaryBuyValue;
    private Double proprietarySellValue;
    private Double proprietaryTotalNetValue;

    private Integer buyOrder;
    private Integer sellOrder;
    private Integer mediumBuyOrder;
    private Integer mediumSellOrder;
    private Integer largeBuyOrder;
    private Integer largeSellOrder;
    private Integer buyOrderVolume;
    private Integer sellOrderVolume;

    private Double totalVolume;
    private String percentageChange;
    private String priceRange;

}
